## Reversi Server

- Create database

```mysql
SET PASSWORD = PASSWORD('abcd1234');
FLUSH PRIVILEGES;
CREATE DATABASE reversi DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;
SET GLOBAL time_zone = '+0:00';
exit;
```