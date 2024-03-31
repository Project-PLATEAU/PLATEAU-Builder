# PLATEAU-Builder（CityGML編集ツール）
![スクリーンショット 2024-02-14 171551](./Documentation/resources/Index/index.png)


## 1. 概要
本リポジトリでは、2023年度のProject PLATEAUで開発した「PLATEAU-Builder」のソースコードを公開しています。  
「PLATEAU-Builder」は、PLATEAUの標準仕様に準拠したCityGMLデータの編集または品質検査を行うためのシステムです。

※本プロジェクトは、令和5年度「都市デジタルツインの実現に向けた研究開発及び実証調査業務」（内閣府/研究開発とSociety5.0との橋渡しプログラム（BRIDGE））の一部として実施されました。

## 2. 「PLATEAU-Builder」について
「PLATEAU-Builder」は、3D都市モデルの整備を業務として実施する測量会社等のみならず、3D都市モデルを利用・作成したい個人や専門分野の異なる測量またはGISの専門ではないベンダーにおいても、簡易に3D都市モデルを作成して研究や検証に利用できることを目的として開発いたしました。

本システムは、建築物モデルの3D都市モデルデータについて、ジオメトリ・地物定義・空間属性・主題属性を編集する機能や外部で作成した3Dデータオブジェクトで置換する機能といった編集機能に加えて、編集したデータをPLATEAUの標準仕様に準拠した基準で品質検査する機能、PLATEAUの標準仕様に準拠したデータセットとして出力する機能を実装しています。  
また、広く一般的に簡易なGUIを備えたオープンソースソフトウェアとして開発されています。

## 3. 利用手順
本システムの構築手順及び利用手順については[操作マニュアル](https://project-plateau.github.io/3PLATEAU-Builder/index.html)を参照してください。

## 4. システム概要
|分類|機能名|機能説明|
|---|---|---|
|インポート|CityGMLインポート機能|PLATEAU標準仕様v3に準拠したCityGML2.0形式のLOD1~3の建築物モデルをインポートする。|
||3Dモデルインポート機能|3D都市モデル内の地物を選択し、OBJ及びGLB/glTF形式の3Dモデルをインポートすることで、選択した地物の形状をインポートしたデータで置き換える。対象は建築物モデルとし、屋根面や底面の検出も行う。|
|表示|3D都市モデル3D描画機能|3D都市モデルを3Dビュー上に描画する。|
||地物一覧表示機能|3D都市モデル内の地物を一覧で表示する。|
||地物詳細情報表示機能|地物の形状情報・地物定義・空間属性・主題属性を表示する。|
|編集|地物詳細情報編集機能|地物の形状情報・地物定義・空間属性・主題属性を変更する。|
||面作成機能|地物の各面を異なる地物として定義する。|
||地物形状操作機能|地物の位置情報を視覚的に操作する。|
|エクスポート|3Dモデルエクスポート機能|3D都市モデル内の地物を選択し、OBJ及びGLB/glTF形式の3Dモデルとしてエクスポートする。|
||品質検査機能|PLATEAUの標準仕様書で定義されている評価パラメータ値を用いて3D都市モデルデータの完全性・論理一貫性・主題正確度を品質検査する。|
||データセットエクスポート機能|3D都市モデルをCityGML2.0形式でエクスポートする。PLATEAU標準に準拠したファイル群（コードリスト・メタデータなど）を生成する。|

## 5. 利用技術
| 種別 | 名称 | バージョン | 詳細 |
| --- | --- | --- | --- |
|プログラミング言語|[java](https://www.java.com/ja/)|11~|プログラミング言語。本ツールは全てjavaで実装する。|
|フレームワーク|[javafx](https://openjfx.io/)|17.0.10|javaのGUIフレームワーク|
|ライブラリ|[citygml4j](https://github.com/citygml4j/citygml4j)|1.4.2|citygml読み込み・書き出しのためのライブラリ|
||[iur-ade-citygml4j](https://github.com/citygml4j/iur-ade-citygml4j)|2.12.0|citygml4jのi-UR向け拡張ライブラリ|
||[java3D](https://www.oracle.com/java/technologies/javase/java-3d.html)|1.3.1|3Dレンダリングライブラリ。3Dレンダリング自体はjavafxで行うため、CityGMLの頂点データのポリゴンメッシュ化のみに利用する。|
||[JglTF](https://github.com/javagl/JglTF)|2.0.3|glTF読み込みライブラリ|

## 6. 動作環境
| 項目 | 最小動作環境 | 推奨動作環境 |
| --- | --- | --- |
|OS|Microsoft Windows 11|同左|
|CPU|Intel Core i3以上|Intel Core i5以上|
|メモリ|8GB|16GB|
|ディスプレイ解像度|1024×768以上|同左|
|ネットワーク|不要|同左|

## 7. 本リポジトリのフォルダ構成
本リポジトリのソースコードはsrc/main/java/org/plateau/citygmleditor内に以下のモジュールごとに配置されています。
| フォルダ名 | 詳細 |
| --- | --- |
|citygmleditor|アプリケーションの初期化処理|
|citymodel|3D都市モデルの可視化・情報保持|
|control|3D都市モデルの操作|
|converters|3Dファイル、CityGML間の変換|
|exporters|各種ファイルへのエクスポート|
|fxml|UI|
|geometry|座標系変換|
|importers|各種ファイルのインポート|
|validation|品質検査|
|world|3D都市モデルの配置空間およびそこに配置するGizmo等のオブジェクト定義|

## 8. ライセンス
- ソースコード及び関連ドキュメントの著作権は国土交通省に帰属します。
- 本ドキュメントは[Project PLATEAUのサイトポリシー](https://www.mlit.go.jp/plateau/site-policy/)（CCBY4.0及び政府標準利用規約2.0）に従い提供されています。

## 9. 注意事項
- 本リポジトリは参考資料として提供しているものです。動作保証は行っていません。
- 本リポジトリについては予告なく変更又は削除をする可能性があります。
- 本リポジトリの利用により生じた損失及び損害等について、国土交通省はいかなる責任も負わないものとします。

## 10. 参考資料
- PLATEAU WebサイトのUse caseページ「CityGML編集ツール」：
- 操作マニュアル：https://synesthesias.github.io/PLATEAU-CityGML-Editor/index.html
