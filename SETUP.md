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
    brew services start mysql@5.7
    ```

    Detailed setup found: [here](https://gist.github.com/jordanluyke/0118cb3d83c7b1f6659f1b4c470920d7#mysql)

    Import/Export
    ```
    mysqldump -uroot -p reversi > reversi.sql
    mysql -uroot -p reversi < reversi.sql
    ```
