## Setup

1. Homebrew

    ```
    /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
    ```

1. Java 8

    ```
    brew tap caskroom/versions
    brew cask install corretto8
    ```
    
1. Maven

    ```
    brew install maven
    ```

1. MySQL

    ```
    brew install mysql@5.7
    brew link mysql@5.7 --force
    ```

    Edit /etc/mysql/my.cnf and add:
    ```
    [mysqld]
    character-set-server=utf8mb4
    collation-server=utf8mb4_general_ci
    wait_timeout=999999999
    interactive_timeout=999999999
    ```

    ```
    brew services start mysql@5.7
    mysql -u root
    mysql> ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
    mysql> SET GLOBAL time_zone = '+0:00';
    mysql> CREATE DATABASE reversi;
    ```
   
    Import/Export
    ```
    mysqldump -uroot -p reversi > reversi.sql
    mysql -uroot -p reversi < reversi.sql
    ```
