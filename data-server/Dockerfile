FROM mhart/alpine-node:7

RUN npm install -g nodemon@1.11.0

COPY package.json /server/package.json

WORKDIR /server

RUN npm install --production

COPY . /server

EXPOSE 8000

CMD npm start