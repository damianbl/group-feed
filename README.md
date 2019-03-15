# Group Feed

## How to start the project

`sbt run` 
- it starts both the server and the file HSQL database

## Technology stack
- Scala 2.12.x
- Akka
    - Actors 2.5.x
    - Http 10.1.x
- Slick 3.3.x
- Hsqldb database 2.4.x

## API overview

### Authorization
All API endpoints require `Authorization` http header with an access token:
`Authorization: AccessToken 7ehrXcp6acX9`

### Create a group

**URL** : `http://localhost:8080/api/group`

**Method** : `POST`

**Body example**

```json
{
    "name": "Scala"
}
```

### Create a user

**URL** : `http://localhost:8080/api/user`

**Method** : `POST`

**Body example**

```json
{
    "name": "Martin Odersky"
}
```

### Become member of a group

**URL** : `http://localhost:8080/api/group/user`

**Method** : `POST`

**Body example**

```json
{
  "groupId": "32bcadb994444acc945a190e232e1062",
  "userId": "910438cb8df14c28bdd3956abd1b9623"
}
```

### Get user groups

**URL** : `http://localhost:8080/api/group/user/{user-id}`

**Method** : `GET`

### Add post to a group

**URL** : `http://localhost:8080/api/post/group/`

**Method** : `POST`

**Body example**

```json
{
  "authorId": "910438cb8df14c28bdd3956abd1b9623",
  "content": "Hello, I'm Martin Odersky.",
  "groupId": "32bcadb994444acc945a190e232e1062"
}
```

### Get group feed

**URL** : `http://localhost:8080/api/feed/group/{group-id}`

**Method** : `GET`

### Get user all groups feed

**URL** : `http://localhost:8080/api/feed/all/{user-id}`

**Method** : `GET`
