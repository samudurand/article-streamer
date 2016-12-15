FROM mhart/alpine-node:7

RUN npm install -g nodemon@1.11.0

COPY . /server

WORKDIR /server

RUN npm install

EXPOSE 8000

CMD npm start