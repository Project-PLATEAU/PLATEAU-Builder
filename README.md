# [WIP]CityGML編集ツール
[適当な画像入れる]()

## 1. 概要
本リポジトリでは、Project PLATEAUにおける都市デジタルツイン実現に向けた研究開発及び実証調査業務の一部であるcm23-08「CityGML編集ツールの開発」について、その成果物である「CityGML編集ツール」のソースコードを公開しています。

「CityGML編集ツール」は、PLATEAU標準仕様に準拠したCityGMLデータの編集または品質検査を行うためのシステムです。

## 2. 「CityGML編集ツール」について
「CityGML編集ツール」は、3D都市モデルの整備を業務として実施する測量会社等のみならず、3D都市モデルを利用・作成したい個人や専門分野の異なる測量またはGISの専門ではないベンダーにおいても、簡易に3D都市モデルを作成して研究や検証に利用できることを目的として開発いたしました。

本システムは、建築物モデルの3D都市モデルデータについて、ジオメトリ・地物定義・空間属性・主題属性を編集する機能や外部で作成した3Dデータオブジェクトで置換する機能といった編集機能に加えて、編集したデータをPLATEAU標準に準拠した基準で品質検査する機能、PLATEAU標準に準拠したデータセットとして出力する機能を実装しています。また、広く一般的に簡易なGUIを備えたオープンソースソフトウェアとして開発されています。

本システムの詳細については[技術検証レポート]()を参照してください。

## 3. 利用手順
本システムの構築手順及び利用手順については[利用チュートリアル]()を参照してください。

## 4. システム概要
### 【インポート】
#### CityGMLインポート機能
- CityGMLデータをツールにインポートすることがします。
- PLATEAU標準仕様v3に準拠したCityGML2.0形式のLOD1～3の建築物モデルデータが対象です。
### 【表示】
#### 3D描画機能
- CityGMLデータを3Dで描画します。
- また、描画したデータの位置情報をマウス操作によって編集します。
#### 地物表示機能
- CityGMLデータ内の地物を一覧で表示します。
- 各地物のジオメトリ情報・地物定義・空間属性・主題属性を表示します。
### 【編集】
#### 地物編集機能
- 各地物のジオメトリ情報・地物定義・空間属性・主題属性を編集します。
### 【品質検査】
#### 品質検査機能
- PLATEAU標準の評価パラメータ値を用いて3D都市モデルデータの完全性・論理一貫性・主題正確度を品質検査します。
### 【エクスポート】
#### データセットエクスポート機能
- CityGMLデータをCityGML2.0形式でエクスポートします。
- PLATEAU標準に準拠したファイル群（コードリスト・メタデータなど）を生成します。

## 5. 利用技術
| 種別 | 名称 | バージョン | 詳細 |
| --- | --- | --- | --- |
|プログラミング言語|[java](https://www.java.com/ja/)||プログラミング言語。本ツールは全てjavaで実装する。|
|フレームワーク|[javafx](https://openjfx.io/)||javaのGUIフレームワーク|
|ライブラリ|[citygml4j](https://github.com/citygml4j/citygml4j)||citygml読み込み・書き出しのためのライブラリ|
||[iur-ade-citygml4j](https://github.com/citygml4j/iur-ade-citygml4j)||citygml4jのiUR向け拡張ライブラリ|
||[java3D](https://www.oracle.com/java/technologies/javase/java-3d.html)||3Dレンダリングライブラリ。3Dレンダリング自体はjavafxで行うため、CityGMLの頂点データのポリゴンメッシュ化のみに利用する。|
||[JglTF](https://github.com/javagl/JglTF)||glTF読み込みライブラリ|

## 6. 動作環境
| 項目 | 最小動作環境 | 推奨動作環境 |
| --- | --- | --- |
|OS|||
|CPU|||
|メモリ|||
|ディスプレイ解像度|||
|ネットワーク|||

## 7. 本リポジトリのフォルダ構成

## 8. ライセンス

## 9. 注意事項

## 8. 参考資料


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
