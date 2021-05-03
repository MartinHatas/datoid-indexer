# Datoid Indexer

Simple indexing service for datoid.cz and search frontend. 

## How to run

### ElasticSearch

```bash
docker run --name datoid-elastic -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "http.cors.allow-origin=http://localhost:3000" -e "http.cors.enabled=true" -e "http.cors.allow-headers=X-Requested-With,X-Auth-Token,Content-Type,Content-Length,Authorization" -e "http.cors.allow-credentials=true"  docker.elastic.co/elasticsearch/elasticsearch:7.12.1
```
