# [WIP]操作マニュアル

## 1. 本書について
本書では、CityGML編集ツール（以下「本ツール」という）の画面・使い方・扱うデータについて記載しています。
- 本ツールの各画面の表示内容や構成の詳細については、「2. 画面」を参照してください。
- 本ツールの使い方については、「3. 使い方」を参照してください。
- 本ツールで扱うインポート/エクスポートデータについては、「4. 扱うデータ」を参照してください。

## 2. 画面

## 2-1. トップ画面
本ツールの実行ファイルを実行すると、以下の画面が表示されます。

![image](https://github.com/Synesthesias/PLATEAU-CityGML-Editor/assets/20107036/7d401e13-ec5d-4806-a0f4-64d681b9f593)


CityGMLデータのインポートを行うと、以下のような表示内容になります。

![スクリーンショット 2024-02-14 161128](https://github.com/Synesthesias/PLATEAU-CityGML-Editor/assets/20107036/59cd38d7-de71-4cec-8765-6837beaba84e)


### ①CityGMLインポートボタン
CityGMLインポートを行います。ボタン押下後に「2-2.CityGMLインポート画面」へ移動します。
### ②書き出しボタン
データセットエクスポートを行います。ボタン押下後に「2-3.データセットエクスポート画面」へ移動します。
### ③品質検査ボタン
品質検査を行います。ボタン押下後に「2-4.品質検査画面」へ移動します。
### ④地物一覧エリア
インポートしたデータの地物一覧を表示します。
### ⑤3Dビューエリア
インポートしたデータを3Dで描画します。
また、上部のメニューボタンを切り替えることで、マウス操作によって地物を移動・回転したり、描画するLODを変更できます。

![スクリーンショット 2024-02-14 162827](https://github.com/Synesthesias/PLATEAU-CityGML-Editor/assets/20107036/0b2ac71f-4cf0-4f84-ba5d-c34e7d9e708c)



### ⑥地物詳細エリア

## 2-2. CityGMLインポート画面

## 2-3. データセットエクスポート画面

## 2-4. 品質検査画面


## 3. 使い方

## 3-1. CityGMLデータをインポートする
トップ画面にて、インポートボタン押下後にインポートするCityGMLデータを選択し、本ツール内にインポートできます。

（キャプチャ）

## 3-2. CityGMLデータを3Dで視覚的に確認する
CityGMLデータのインポートを行うと、トップ画面-3Dビューエリアにて、各地物を3Dにて視覚的に確認できます。
また、マウス操作によって各地物を回転・移動・拡大縮小など行うことができます。

（キャプチャ）

## 3-3. 地物に3Dオブジェクトを当てはめる
他ツールなどで作成済みの3Dオブジェクトを地物に当てはめることができます。
地物一覧エリアにて該当地物の～～～

（キャプチャ）

## 3-4. 地物の属性を編集する
地物一覧エリアにて該当地物を選択すると、地物詳細エリアに詳細情報が表示されます。
～～～

（キャプチャ）

## 3-5. 地物の面（屋根面・壁面など）を編集する
複数メッシュをまとめて屋根面や壁面などとして定義することができます。
地物一覧エリアにて～～～

（キャプチャ）

## 3-6. CityGMLデータを品質検査する
トップ画面にて、品質検査ボタン押下後に品質検査パラメータと出力フォルダを選択し、品質検査することができます。
画面内にも品質検査結果のログが出力され、結果確認できます。

（キャプチャ）

## 3-7. CityGMLのデータセットをエクスポートする
トップ画面にて、書き出しボタン押下後に出力フォルダを選択し、PLATEAU標準のデータセットをエクスポートできます。

（キャプチャ）

## 4. 扱うデータ
## 4-1. CityGMLデータ
CityGMLインポート機能でインポートできるデータは以下のとおりです。
| フォルダ階層 | 内容 |
|---|---|

## 4-2. 3Dオブジェクトデータ
3Dオブジェクトインポート機能でインポートできるデータは以下のとおりです。
| フォルダ階層 | 内容 |
|---|---|

## 4-3. データセット
データセットエクスポート機能で出力されるデータは以下のとおりです。
| フォルダ階層 | 内容 |
|---|---|
|codelists|3D都市モデル標準製品仕様書Ver.3.2で定義されているコード値ファイルと、インポートしたCityGMLデータに含まれるコード値ファイル|
|metadata|インポートしたCityGMLデータに含まれるmetadataファイルをもとに、日付やデータに含まれる地物型/LODの値のみ変更したファイル|
|schemas|インポートしたCityGMLデータに含まれるschemasファイル|
|specification|インポートしたCityGMLデータに含まれるspecificationファイル|
|udx|インポートしたCityGMLデータに含まれる全ての地物の3D都市モデルファイル|
