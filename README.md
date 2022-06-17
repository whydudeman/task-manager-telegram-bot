# Telegram Task Manager 

Telegram Task Manager is simple demo project for sending notification from endpoints to telegram chat

Helps employees to manage their tasks right from telegram 

## Getting Started

There is two Controllers for tasks and users

To start using it should be created User with phone which is main phone in your telegram account
using Post request http://localhost:8080/users/
```json
{
    "name" : "test_user",
    "username" : "test_username",
    "phone" : "+77771112233"
}
```

After getting "id" from response, you can create Task and assign this task to just created user
```json
{
    "id": "62ac711a517a3907db8088e2",
    "name": "test_user",
    "username" : "test_username",
    "phone" : "+77771112233"
}
```

## Creating Task / Updating
There is endpoint for creating task by using this post request localhost:8080/trigger/add-new-task
```json
{
    "name" : "Test Name",
    "projectName" : "Test Project",
    "status" : "IN_PROGRESS",
    "executorId" : "62ac711a517a3907db8088e2", <-This is id we got from user
    "deadline" : " 2022-06-17 23:55",
    "description" : "Test Description",
    "difficulty" : 1,
    "priority" : "HIGH"
}
```
The same way as we created Task we can update it using response id
```json
{
    "id": "6"
}
```

To update Task you can take this id then use it to put request localhost:8080/trigger/update-task/6 the same way we create the previous task

## Trigger to send All tasks of user
There is also endpoints for sending all available tasks of user to telegram using this request

localhost:8080/trigger/send-all-tasks/62ac711a517a3907db8088e2
## Telegram Bot interaction
After creating a user u can open bot then click start command 

```python
import foobar

# returns 'words'
foobar.pluralize('word')

# returns 'geese'
foobar.pluralize('goose')

# returns 'phenomenon'
foobar.singularize('phenomena')
```

## License
[No License]()