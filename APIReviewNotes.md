# Logged in as user

User:
```
    {
      "id": "6e7e9b5a-1b2c-4aa8-9e86-3dbe49f0f101",
      "username": "john.doe",
      "enabled": true,
      "emailVerified": true,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@shelfinity.com",
      "credentials": [{ "type": "password", "value": "john123", "temporary": false }],
      "realmRoles": ["user"]
    }
```

Token is generated using the script `kc-token.sh`

```
{
  "exp": 1757174267,
  "iat": 1757173967,
  "auth_time": 1757173728,
  "jti": "b3328a74-ece7-46ce-9081-d2a71580b95b",
  "iss": "http://localhost:8080/realms/shelfinity",
  "aud": "shelfinity-backend",
  "sub": "6e7e9b5a-1b2c-4aa8-9e86-3dbe49f0f101",
  "typ": "Bearer",
  "azp": "shelfinity-frontend",
  "session_state": "87c1a46b-5c04-4b10-8d0d-40335d29197b",
  "acr": "1",
  "allowed-origins": [
    "*"
  ],
  "realm_access": {
    "roles": [
      "user"
    ]
  },
  "scope": "openid email profile",
  "sid": "87c1a46b-5c04-4b10-8d0d-40335d29197b",
  "email_verified": true,
  "name": "John Doe",
  "groups": [
    "user"
  ],
  "preferred_username": "john.doe",
  "given_name": "John",
  "family_name": "Doe",
  "email": "john.doe@shelfinity.com"
}
iat: 1757173967
  iat (UTC): Saturday 06 September 2025 03:52:47 PM UTC
  iat (IST):  Saturday 06 September 2025 09:22:47 PM IST
exp: 1757174267
  exp (UTC): Saturday 06 September 2025 03:57:47 PM UTC
  exp (IST):  Saturday 06 September 2025 09:27:47 PM IST
auth_time: 1757173728
  auth_time (UTC): Saturday 06 September 2025 03:48:48 PM UTC
  auth_time (IST):  Saturday 06 September 2025 09:18:48 PM IST

```

## 1. GET /books

curl
```
curl -X 'GET' \
  'http://localhost:9080/shelfinity-backend/books' \
  -H 'accept: application/json'
```

sample output:

```
[
{
    "author": "George Orwell",
    "available": true,
    "availableCopies": 1,
    "createdAt": "2025-09-06T14:09:42.390008",
    "description": "A dystopian novel about totalitarianism and surveillance society, following the life of Winston Smith.",
    "id": "56509b69-d773-4d8c-941f-042d8991bac0",
    "isbn": "978-0-14-028333-4",
    "title": "1984",
    "totalCopies": 3,
    "updatedAt": "2025-09-06T14:09:42.390008"
  },
  {
    "author": "Ernest Cline",
    "available": true,
    "availableCopies": 1,
    "createdAt": "2025-09-06T14:09:42.390008",
    "description": "A novel about a teenager who discovers that a video game he plays is actually a training simulation for an alien invasion.",
    "id": "22d6ce3d-27c8-403e-ab9a-6db591e70ed1",
    "isbn": "978-0-316-06857-5",
    "title": "Armada",
    "totalCopies": 3,
    "updatedAt": "2025-09-06T14:09:42.390008"
  }
]
```

The output is truncated. There was more than a dozen books it seems to be in the db, thanks to the seed data. Anyway, I have the below comments:
1. The `availableOnly` parameter seems not to be working. No matter whether it is `true` or `false` the API is always returns all the data. 
2. There should be pagination properties in the response data with `total_count`, `offset` and `limit`. This response structure should be extended to all APIs that returns an array of data. 
3. We do need some way to identify individual copies; currently, we have a `totalCopies` parameter in the response body, but that would not be enough. We would be issueing individual copies to the users. So, there should be a way to map each book item - each copy - to the user who borrowed it. 
4. Seems like the API doesn't have an auth implementation. Only a user with role user should be able to view books.

The API seems RESTFul - viewing the book entity. Anyway, we need to redefine this entity in such a way each copies get a unique id and preserving the grouping of copies. 

## 2. POST /books

Logged out as John Doe and tried to hit the endpoint.

curl

```
curl -X 'POST' \
  'http://localhost:9080/shelfinity-backend/books' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "isbn": "978-0-679-72327-6",
  "description": "A novel about the American Dream and the Jazz Age.",
  "genre": "CLASSIC",
  "publicationYear": 1925,
  "totalCopies": 3
}'
```

Got the below response, 

```
{
  "error": "Admin access required"
}
```
looks good, but I do belive it should first check if the user is logged in. ie, for non user, or no token, it should be 401. 

The request body looks good:
```
{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "isbn": "978-0-679-72327-6",
  "description": "A novel about the American Dream and the Jazz Age.",
  "genre": "CLASSIC",
  "publicationYear": 1925,
  "totalCopies": 3
}
```
This API should not be accessible to a non admin user. So, no comments from a user's perspective. 

## 3. GET /books/available

No need for a separate API. This can be clubbed with the `GET /books` API. We already have a parameter in that API for filtering the available books, though it isn't working unfortunately. A book is  available when atleast a copy of it is available. Need a mechanism that updates the available copies when copy of a book is issued to someone and the book entity is patched with new copies. How about keeping each copies in the book entity itself, and each copy item to have fields issued to, which can be null, or a user id, depending on to whom it is assigned? In that way, issueing a book could be simply patching the book entity.

## 4. GET /books/search

Same comment as above.This can be clubbed with `GET /books` with appropriate search parameters. Need a few more search parameters that a realtime user in a library would relay on - like `title`, `description`, `author`, `isbn`, etc. It would be nice, if we have a an AI based text analyser that simply compare the description the user enterr with the descriptions of the books and find books that almost matches. Not to mention, the current description filter is not working. 

## 5. /books/{id}

curl
```
curl -X 'GET' \
  'http://localhost:9080/shelfinity-backend/books/56509b69-d773-4d8c-941f-042d8991bac0' \
  -H 'accept: application/json'
```

response
```
{
  "author": "George Orwell",
  "available": true,
  "availableCopies": 1,
  "createdAt": "2025-09-06T14:09:42.390008",
  "description": "A dystopian novel about totalitarianism and surveillance society, following the life of Winston Smith.",
  "id": "56509b69-d773-4d8c-941f-042d8991bac0",
  "isbn": "978-0-14-028333-4",
  "title": "1984",
  "totalCopies": 3,
  "updatedAt": "2025-09-06T14:09:42.390008"
}
```

This API looks good generally. The only comment I have is to make this only accessible to the users. 

## 6. PUT /books/{id}

This is an admin API. The normal user John is getting a 403. That is fine, but a non user or unauthorised user or a user whose token expired should get a 401. 

## 7. DELETE /books

This is also be an Admin API, so the comments are same as above. 

> There should be PATCH /books/{id} endpoint to patch book entity. For issuing copies to users, updating availability data etc. It should also be possible to delete a copy incase someone lost it. This PATCH API should also be an admin API. 

## 8. GET /health

No comments - Looks good!

## 9. GET /queues

Admin API; John is getting 403, which is good. 

```
{
  "error": "Admin access required"
}
```

An unauthorised or expired token should get a 401. 

There should be a filter for filtering queue items with the type; what kind of request is in the queue - An admin may want to list all the registration requests.

## 10. POST /queues

This is a user API, and it should be a user API; because it is the users who queue the requests. 

curl
```
curl -X 'POST' \
  'http://localhost:9080/shelfinity-backend/queues' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiw
  <-------------token------------->
  CDntV8beYsKL0OHxT-mEmcaOWT4kLyaeoio-NQ8QLY8j6_Eoj9thYs7mYRQXZJSV0BhPg' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "BORROW",
  "userKeycloakId": "y",
  "bookId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "description": "6"
}'
```

for the above request, I got a 500 error:

```
can't parse JSON.  Raw result:

Internal error: No enum constant com.shelfinity.queues.QueueType.BORROW
```

We should have proper exception handling for these types of scenarios - I would say, we should need Jakarta validation for validating inputs before they reach to the service layer. 

When I fixed that parameter, I was able to create a queue item.

```
{
  "bookId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "createdAt": "2025-09-06T17:01:52.937980932",
  "description": "6",
  "id": "d09b8066-1a39-484a-8fce-0a894f000951",
  "status": "PENDING",
  "type": "BOOK_BORROW",
  "userKeycloakId": "6e7e9b5a-1b2c-4aa8-9e86-3dbe49f0f101"
}
```

It is great that the keyclock id is being fetched from the DB even though the request body allow to specify it. In fact, we don't need keyclock id here at all. It is mapped to the user id via the user's table. So, user id would be enough to track a queue resource. And, a queue item can also be a user registraction request. So, it make sense to have keyclock id as a parameter. But, we should have separate request body for each type of requests to avoid confusion and mess. These types should also be validated strictly.

An existing user should not be able to submit a registration request - a registration request must be comes through keyclock with a valid keyclock id. For now, it is safe to assume that the user whould have set up his keyclock account before submitting the registration request, because the flow us via the UI. 

For submitting registration request, a user must be authorised - he should have a valid keyclock account and must be able to generate a token that the backend could verify

## 11. GET /queues/my

curl

```
curl -X 'GET' \
  'http://localhost:9080/shelfinity-backend/queues/my' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiw
  <-------------- token -------------->
  WlTlOeNzvFPPDEiwRfFuahpjUwJmUg'
```

response

```
[
  {
    "bookId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "createdAt": "2025-09-06T17:01:52.937980932",
    "description": "6",
    "id": "d09b8066-1a39-484a-8fce-0a894f000951",
    "status": "PENDING",
    "type": "BOOK_BORROW",
    "userKeycloakId": "6e7e9b5a-1b2c-4aa8-9e86-3dbe49f0f101"
  }
]
```

We need this API, for fetching the queue item of the current user. But it isn't looks very RESTful. This can be merged with the first API - /queues with appropriate parameters. Like a userid parameter - the current user should only fetch his own items. He should not be able to featch items of anyone else. He should also be able to filter queue items of him. Anyway, the paramter id should be required for non admin users. They should get 403, if try to fetch queues of all or any of the other users. Only the admin should be able to view the whole queue.

## 12. GET /queues/{id}

Same comments as above. A user should only be able to fecth his queue items. This API can also be clubbed with the `GET /queues` API with a suitable parameter. 

## 13. DELETE /queues/{id}

This looks like an admin API. But, I would say a user should also be able to delete his queue items. An admin should be able to remove anyone's queue items.

## 14. PATCH /queues/{id}/status

This should be simply `PATCH /queues/{id}` to be RESTful. And this should be an admin API. 

We need validation for `status` field. 

## 15. GET /users

This is an Admin API as it supposed to be. Only the admin should be able to see all the users. 

## 16. POST /users

There should not be a POST endpoint for resource user. A user creation must be through queues. When an admin approves a registration request, a new user should be created and the item should be removed from the queue. It may not sound very RESTful, but here the situation fits with the exception. So, no need of this API. 

## 17. GET users/me

Not RESTful. We can club this with the first user API with the same logic explained for queues. A user should only be able to view hist profile. If he try to view anyone else, he should get a 403. 

### 18. GET /users/{id}

Same as explained for queues. We can club this and the previous one with the GET /users API with a parameter user id. Or, It wouldn't be nice to have separate API to fetch userid. So, it is okay to have a search parameter for fetching current user. An enum with values current and all. Only the admin should be able to fetch all users - same can be applied to queues.

### 19. PUT /users/{id}

Do a user update their entire profile at once? I would say no. Anyway, they should be able to edit fields like email and phone number. But I would prefer those values to be managed by keyclock. How to have the keyclock and the app to be linked so that the app don't have to store them and only to be fetch those data from the keyclock db or API?

Anyway, I would say, a user should able to modify the items he enter to the app. And, an admin should be able to patch a user account with the `enable` falg so that he could suspend an account. A suspended account should not be able to do any operation until it is being enabled by an admin. So, the user entity should have fields enabled and it should be valudated along with the token before performing any actions. 

We should also store the last log in time stamp in the user entity.

### 20. DELETE users/{id}

I would say a user account deletion should also be go through the queues flow. A user should submit a account deletion request, that an admin could review and approve and when he approve, the item should be deleted.

Anyway, an admin can delee a user directly, because there is no point in adding it to the queue for him to review it. So, we should have an API for that.