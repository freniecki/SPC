How Aleksiej launches the application:

1. Start localstack
```
sudo docker run --rm -it -p 4566:4566 -p 4510-4559:4510-4559 -e SERVICES="s3,cognito-idp,logs" -e DEBUG=1 --name localstack localstack/localstack --network="host"
```
```
sudo docker exec -it localstack bash
IN LOCALSTACK:  awslocal s3api create-bucket --bucket user-file-bucket
```

2. Start postgreSQL database. Script:
```
 kirsche@bash: cat ./pg_docker_run.sh
  
docker run -d \
  --name postgres_spc \
  -p 5432:5432 \
  -e POSTGRES_USER=spc \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=spc \
  postgres:latest
```

3.`kirsche@bash sudo docker stop postgres_spc && sudo docker rm postgres_spc && sudo ./pg_docker_run.sh`

4. Launch the java thing.
5. 
6. GucciGang GucciGang GucciGang GucciGang 
