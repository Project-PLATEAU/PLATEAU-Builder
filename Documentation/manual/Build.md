## ビルド手順
ソースコードからビルドする場合、以下の手順に従ってください。

###  事前準備
- gradleのインストール
    - [こちらの手順でインストール](https://www.kkaneko.jp/tools/win/gradle.html)

### セットアップ
以下コマンド等でローカルにクローンします。
```
git clone https://github.com/Project-PLATEAU/PLATEAU-Builder
```

また、ビルドの際は[GDAL](https://gdal.org/)が必要となります。
- [こちらの手順でインストール](https://www.kkaneko.jp/db/win/gisinternals.html)
- Older Releasesより3.8.0のcoreをダウンロードしてください。
- 標準インストールディレクトリから変更した場合は`plateaubuilder-gui/build.gradle`のプロパティを変更する必要があります。
```
ext {
    ...
    gdalInstallDir = 'C:/Program Files/GDAL' // GDALインストール先を指定
}
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
- exeファイル生成時には必要なGDALライブラリがコピーされます。

