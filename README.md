ものすごく最小限なミニブログ
=================================

このアプリケーションは試験的に作られたものすごく機能が乏しいミニブログWebアプリケーションです。

Requirements
===========

- scala 及び sbt

- PostgreSQL

How to Setup 
=======

```
$ git clone git@github.com:qwerty2501/scala-examination.git
$ cd scala-examination/
```
下記を参考に、PostgreSQLのユーザとデータベースを作成  
https://www.postgresql.jp/document/9.6/html/app-createuser.html  
https://www.postgresql.jp/document/9.3/html/app-createdb.html
conf/application.confを開き、下記設定を変更します
```
slick.dbs.default.db.url="jdbc:postgresql://{ホスト名}/{作成したデータベース名}"
slick.dbs.default.db.user={作成したデータベースユーザ名}
slick.dbs.default.db.password="{作成したデータベースユーザパスワード}"
```

How to Run
====
scala-examination/にて、下記コマンドを実行
```
$ cd {git cloneしたディレクトリ}scala-examination/
$ sbt run
```
