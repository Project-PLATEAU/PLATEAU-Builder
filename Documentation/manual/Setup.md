# 環境構築手順書

## 1. 本書について
本書では、CityGML編集ツール（以下「本ツール」という）の利用環境構築手順について記載しています。

本ツールの構成や仕様の詳細については[技術検証レポート]()も参考にしてください。

## 2. 動作環境
| 項目 | 最小動作環境 | 推奨動作環境 |
| --- | --- | --- |
|OS|Microsoft Windows 11|同左|
|CPU|Intel Core i3以上|Intel Core i5以上|
|メモリ|4GB|16GB|
|ディスプレイ解像度|1024×768以上|同左|
|ネットワーク|不要|同左|

## 3. インストール手順
Githubリポジトリの[リリース](https://github.com/Synesthesias/PLATEAU-CityGML-Editor/releases)から最新版のリリースファイルをダウンロードします。

ファイル内にあるexeファイルを実行することで本ツールを利用できます。

## 4. ビルド手順
###  事前準備
- gradleのインストール
  - [こちらの手順でインストール](https://www.kkaneko.jp/tools/win/gradle.html)

### セットアップ
以下コマンド等でローカルにクローン
```
git clone https://github.com/Synesthesias/PLATEAU-CityGML-Editor
```

### 実行
PLATEAU-CityGML-Editorフォルダ内で以下を実行します。
```
gradle run
```

### ※exe生成
PLATEAU-CityGML-Editorフォルダ内で以下を実行します。
```
gradle deploy
```
`build/product`配下にexeファイルを含んだ配布用ディレクトリが生成される。


