## Reversi

1. Install homebrew

    ```
    /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
    ```

1. Install Java 8

    ```
    brew tap caskroom/versions
    brew cask install java8
    ```
    
    ```
    sudo apt-get install openjdk-8-jre-headless
    ```
 
1. Enable unlimited strength JCE

    Open file:

    ```
    $JAVE_HOME/jre/lib/security/java.security
    ```

    Uncomment the line:

    ```
    crypto.policy=unlimited
    ```

1. Install Maven

    ```
    brew install maven
    ```

1. Download and set up MySQL

    ```
    brew install mysql@5.7
    brew link mysql@5.7 --force
    brew services start mysql@5.7
    ```

    1. Once the MySQL server has started open a new terminal and run the command `mysql -u root`

    1. Run the following SQL commands:

    ```mysql
    # SET PASSWORD = PASSWORD('abcd1234');
    ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
    SET GLOBAL time_zone = '+0:00';
    CREATE DATABASE reversi DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;
    ```
