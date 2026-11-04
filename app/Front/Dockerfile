FROM node:22-alpine AS dev

WORKDIR /workspace
EXPOSE 5173

CMD ["sh", "-c", "npm ci && npm run dev -- --host 0.0.0.0"]

FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ARG VITE_TOKEN_KEY=sesam-token
ENV VITE_API_URL=$VITE_API_URL VITE_TOKEN_KEY=$VITE_TOKEN_KEY
RUN npm run build

FROM nginx:1.26-alpine AS prod
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
