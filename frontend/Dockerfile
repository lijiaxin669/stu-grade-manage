# Build stage
FROM node:22.22.0-alpine3.22 AS build-stage
WORKDIR /app

# Use npmmirror registry to speed up dependency resolution in CN networks
RUN npm config set registry https://registry.npmmirror.com

COPY package*.json ./
RUN npm install -g pnpm@9 && pnpm config set registry https://registry.npmmirror.com
COPY pnpm-lock.yaml ./
RUN pnpm install --config.confirmModulesPurge=false
COPY . .
RUN pnpm build

# Production stage
FROM nginx:stable-alpine AS production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
