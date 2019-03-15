# Group Feed

#How to start the project

`sbt run` 
- it starts both the server and the file HSQL database


# API overview

### Create group

**URL** : `http://localhost:8080/api/group`

**Method** : `POST`

**Body example**

```json
{
    "name": "Scala"
}
```

### Create user

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
