FROM nginx:1.27-alpine

# Eliminar la configuración por defecto de Nginx
RUN rm -rf /etc/nginx/conf.d/default.conf

# Copiar la configuración personalizada con reverse proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar los recursos estáticos del frontend PWA
COPY . /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
