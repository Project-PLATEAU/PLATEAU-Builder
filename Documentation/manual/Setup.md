# 環境構築手順書

## 1. 本書について
本書では、PLATEAU Builder（以下「本ツール」という）の利用環境構築手順について記載しています。

## 2. 動作環境
| 項目 | 最小動作環境 | 推奨動作環境 |
| --- | --- | --- |
|OS|Microsoft Windows 11|同左|
|CPU|Intel Core i3以上|Intel Core i5以上|
|メモリ|4GB|16GB|
|ディスプレイ解像度|1024×768以上|同左|
|ネットワーク|不要|同左|

## 3. インストール手順
Githubリポジトリの[リリース](https://github.com/Project-PLATEAU/PLATEAU-Builder/releases)から最新版のリリースファイルをダウンロードします。

ファイル内にあるexeファイルを実行することで本ツールを利用できます。

## 4. ビルド手順
ソースコードからビルドする場合、以下の手順に従ってください。

###  事前準備
- gradleのインストール
  - [こちらの手順でインストール](https://www.kkaneko.jp/tools/win/gradle.html)

### セットアップ
以下コマンド等でローカルにクローンします。
```
git clone https://github.com/Project-PLATEAU/PLATEAU-Builder
```

### 実行
PLATEAU-Builderフォルダ内で以下を実行します。
```
gradle plateaubuilder-gui:run
```

### exeファイル生成
PLATEAU-Builderフォルダ内で以下を実行します。
```
gradle deploy
```
`plateaubuilder-gui/build/product`配下にexeファイルを含んだ配布用ディレクトリが生成されます。


