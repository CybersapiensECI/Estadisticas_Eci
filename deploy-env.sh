#!/bin/bash
set -e

ENV=$1
if [ -z "$ENV" ]; then
    echo "ERROR: Debe especificar el ambiente (qa o prod)."
    echo "Uso: ./deploy-env.sh <qa|prod>"
    exit 1
fi

echo "================================================"
echo "  Estadisticas_Eci - Ambiente: $ENV"
echo "================================================"

# ============ CONFIG ============
# Default to sharing the same resource group and environment to respect Azure Student limits
RESOURCE_GROUP=${AZURE_RESOURCE_GROUP:-"cybersapiens-rg"}
LOCATION=${AZURE_LOCATION:-"eastus"}
ACA_ENV=${AZURE_ACA_ENV:-"cybersapiens-env"}
PG_NAME=${AZURE_PG_NAME:-"cybersapiens-pg"}
PG_PASS=${PG_PASSWORD:-"CyberSapiens2024!"}

# ============ RESOURCE GROUP ============
if ! az group show --name $RESOURCE_GROUP >/dev/null 2>&1; then
    echo ">>> Creando Resource Group $RESOURCE_GROUP..."
    az group create --name $RESOURCE_GROUP --location $LOCATION
else
    echo ">>> Reutilizando Resource Group: $RESOURCE_GROUP"
fi

# ============ CONTAINER REGISTRY ============
# If ACR_NAME is passed and already exists in Azure, use it without creating it
if [ -n "$ACR_NAME" ] && az acr show --name "$ACR_NAME" >/dev/null 2>&1; then
    echo ">>> Reutilizando ACR existente globalmente: $ACR_NAME"
else
    EXISTING_ACR=$(az acr list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv)
    if [ -n "$EXISTING_ACR" ]; then
        ACR_NAME=$EXISTING_ACR
        echo ">>> Reutilizando ACR existente en el grupo: $ACR_NAME"
    else
        if [ -z "$ACR_NAME" ]; then
            RANDOM_VAL=$((RANDOM % 90000 + 10000))
            ACR_NAME="cybersapiensacr${ENV}${RANDOM_VAL}"
        fi
        echo ">>> Creando Azure Container Registry: $ACR_NAME"
        az acr create --resource-group $RESOURCE_GROUP --name $ACR_NAME --sku Basic --admin-enabled true
    fi
fi

# ============ ACA ENVIRONMENT ============
if ! az containerapp env show --name $ACA_ENV --resource-group $RESOURCE_GROUP >/dev/null 2>&1; then
    echo ">>> Creando Container Apps Environment: $ACA_ENV..."
    az containerapp env create --name $ACA_ENV --resource-group $RESOURCE_GROUP --location $LOCATION
else
    echo ">>> Reutilizando Container Apps Environment: $ACA_ENV"
fi

# ============ POSTGRESQL ============
EXISTING_PG=$(az postgres flexible-server list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv)
if [ -n "$EXISTING_PG" ]; then
    PG_NAME=$EXISTING_PG
    echo ">>> Reutilizando PostgreSQL Server existente: $PG_NAME"
else
    echo ">>> Creando PostgreSQL Flexible Server: $PG_NAME (B1ms) en $LOCATION..."
    az postgres flexible-server create \
      --resource-group $RESOURCE_GROUP \
      --name $PG_NAME \
      --location $LOCATION \
      --sku-name Standard_B1ms --tier Burstable \
      --storage-size 32 \
      --admin-user postgres \
      --admin-password "$PG_PASS" \
      --public-access 0.0.0.0 \
      --yes
fi

# ============ DATABASES ============
if [ "$ENV" = "prod" ]; then
    ESTA_DB="estadisticas_db"
else
    ESTA_DB="estadisticas_db_$ENV"
fi

echo ">>> Asegurando base de datos $ESTA_DB..."
az postgres flexible-server db create \
  --resource-group $RESOURCE_GROUP \
  --server-name $PG_NAME \
  -n $ESTA_DB || true

echo ">>> Configurando regla de firewall AllowAzureServices..."
az postgres flexible-server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server-name $PG_NAME \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0 || true

PG_HOST=$(az postgres flexible-server show -g $RESOURCE_GROUP -n $PG_NAME --query fullyQualifiedDomainName -o tsv)
echo ">>> PostgreSQL host: $PG_HOST"

# ============ BUILD & PUSH IMAGES ============
echo ">>> Iniciando sesión en Azure Container Registry..."
az acr login --name $ACR_NAME

IMAGE_TAG="$ENV"

echo ">>> Construyendo Estadisticas_Eci ($IMAGE_TAG)..."
docker build -t "$ACR_NAME.azurecr.io/estadisticas-eci:$IMAGE_TAG" -f Dockerfile .

echo ">>> Subiendo Estadisticas_Eci..."
docker push "$ACR_NAME.azurecr.io/estadisticas-eci:$IMAGE_TAG"

# ============ ESTABLECER NOMBRES POR AMBIENTE ============
if [ "$ENV" = "prod" ]; then
    GAMIFICATION_APP_NAME="gamification-service"
    ESTADISTICAS_APP_NAME="estadisticas-eci"
else
    GAMIFICATION_APP_NAME="gamification-service-$ENV"
    ESTADISTICAS_APP_NAME="estadisticas-eci-$ENV"
fi

# ============ GET GAMIFICATION URL ============
echo ">>> Obteniendo URL de GamificationService..."
GAMI_URL=$(az containerapp show -g $RESOURCE_GROUP -n $GAMIFICATION_APP_NAME --query properties.configuration.ingress.fqdn -o tsv || echo "")
if [ -z "$GAMI_URL" ]; then
    echo "WARNING: No se pudo obtener la URL de GamificationService ($GAMIFICATION_APP_NAME). Se usará localhost como fallback."
    GAMI_URL="localhost:8080"
fi

# ============ ESTADISTICAS ECI ============
echo ">>> Desplegando Estadisticas_Eci ($ESTADISTICAS_APP_NAME)..."
az containerapp create \
  --resource-group $RESOURCE_GROUP \
  --environment $ACA_ENV \
  --name $ESTADISTICAS_APP_NAME \
  --image "$ACR_NAME.azurecr.io/estadisticas-eci:$IMAGE_TAG" \
  --registry-server "$ACR_NAME.azurecr.io" \
  --cpu 0.25 --memory 0.5Gi \
  --target-port 8082 \
  --ingress external \
  --min-replicas 0 --max-replicas 2 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=postgres \
    SPRING_DATASOURCE_URL="jdbc:postgresql://$PG_HOST:5432/$ESTA_DB?sslmode=require" \
    DB_USER=postgres \
    DB_PASSWORD="$PG_PASS" \
    SERVICES_GAMIFICATION_URL="https://$GAMI_URL" \
    PROFILE_SERVICE_URL="https://alphaeci-profile-service-prod.icycoast-fc5305af.eastus.azurecontainerapps.io"

ESTA_URL=$(az containerapp show -g $RESOURCE_GROUP -n $ESTADISTICAS_APP_NAME --query properties.configuration.ingress.fqdn -o tsv)

echo ""
echo "================================================"
echo "  Despliegue de Estadisticas_Eci Completado!"
echo "================================================"
echo "Estadisticas_Eci:     https://$ESTA_URL"
echo "Swagger Estadisticas: https://$ESTA_URL/swagger-ui/index.html"
echo "================================================"
echo ""
