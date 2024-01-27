# PLATEAU CityGML Editor
- CityGML編集ツール
- [3DViewer](https://github.com/chirontt/3DViewer)をベースとして作成(←あとで消す)

## 動作環境
- Windows 10, 11
- MacOS(未検証)

## 事前準備
- gradleのインストール
  - [こちらの手順でインストール](https://www.kkaneko.jp/tools/win/gradle.html)

## セットアップ
以下コマンド等でローカルにクローン
```
git clone https://github.com/Synesthesias/PLATEAU-CityGML-Editor
```

## 実行
PLATEAU-CityGML-Editorフォルダ内で以下を実行
```
gradle run
```

## Quick Start
実行後に`Import GML...`→`src/test/resources/org/plateau/citygmleditor/gml/13100_tokyo23-ku_2022_citygml_1_3_op/udx/bldg/53392633_bldg_6697_2_op.gml`を選択

## exe生成
PLATEAU-CityGML-Editorフォルダ内で以下を実行
```
gradle deploy
```

`build/product`配下にexeファイルを含んだ配布用ディレクトリが生成される。

## IDE
- Eclipse, IntelliJ IDEA, vscode等gradle対応しているものなら使用可能
