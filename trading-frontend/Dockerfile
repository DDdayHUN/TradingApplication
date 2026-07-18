FROM node:20-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY  . .
RUN npm run build
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173", "--strictPort"]