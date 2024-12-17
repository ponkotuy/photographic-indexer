# build stage
FROM node:22.12.0-bullseye-slim
WORKDIR /root
COPY . .
RUN npm ci
RUN npm run build

# production stage
FROM node:22.12.0-bullseye-slim
WORKDIR /root
COPY --from=0 /root/build build
COPY package*.json .
RUN npm ci --only=production
EXPOSE 3000
CMD node build
