# 動作環境と対応地物

このページでは、**PLATEAU Builder** を快適にご利用いただくための動作環境や、CityGMLインポート時にサポートされる地物・LOD、さらに対応する3Dファイルやデータセット構成について説明します。

---

## 1. 動作環境

本ツールを正常に動作させるには、以下の要件を満たすマシンが必要です。特に大規模データを扱う場合や複数の3Dファイルを同時に表示する場合は、推奨環境以上をお使いください。

| 項目            | 最小動作環境                  | 推奨動作環境              |
|---------------|-------------------------|---------------------|
| **OS**        | Windows 11 (64bit)      | 同左                  |
| **CPU**       | Intel Core i3 以上        | Intel Core i5 以上    |
| **メモリ**       | 4GB                     | 16GB                |
| **ディスプレイ解像度** | 1024 × 768 以上           | フルHD (1920×1080) 以上 |
| **ネットワーク**    | ベースマップ表示などでインターネット接続が必要 | 同左                  |

> [!NOTE]
> Mac や Linux 等の環境は将来的な対応を検討中です。現状では Windows での利用を推奨します。

---

## 2. CityGMLでサポートされる地物

CityGMLインポート機能では、下表に示す地物やLODを取り扱えます。

| 地物             | LOD0                               | LOD1                                 | LOD2                                 | LOD3                                 | LOD4                               |
|----------------|------------------------------------|--------------------------------------|--------------------------------------|--------------------------------------|------------------------------------|
| **建築物**        | <span style="color: red;">✗</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | <span style="color: red;">✗</span> |
| **交通（道路）**     | <span style="color: red;">✗</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | -                                  |
| **交通（徒歩道）**    | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **交通（広場）**     | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **交通（鉄道）**     | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **交通（航路）**     | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **都市計画決定情報**   | -                                  | <span style="color: green;">✓</span> | -                                    | -                                    | -                                  |
| **土地利用**       | -                                  | <span style="color: green;">✓</span> | -                                    | -                                    | -                                  |
| **災害リスク**      | -                                  | <span style="color: green;">✓</span> | -                                    | -                                    | -                                  |
| **都市設備**       | <span style="color: red;">✗</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | -                                  |
| **植生**         | <span style="color: red;">✗</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | <span style="color: green;">✓</span> | -                                  |
| **水部**         | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **地形**         | -                                  | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **橋梁**         | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span> |
| **トンネル**       | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span> |
| **その他の構造物**    | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **地下街**        | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span> |
| **地下埋設物**      | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span> |
| **区域**         | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | -                                  |
| **汎用都市オブジェクト** | <span style="color: red;">✗</span> | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span>   | <span style="color: red;">✗</span> |

> [!NOTE]
> 上記の対応状況は一例であり、将来的にアップデートされる可能性があります。

---

## 3. 3Dファイル対応

### 3Dファイルインポート
- **OBJ形式**
- **glTF形式**

外部のDCCツール（Blender, Maya, 3ds Max など）で編集した3Dファイルをインポートし、地物の形状を更新可能です。詳細な手順は「[形状情報の編集](EditGeometry.md)」を参照してください。

### 3Dファイルエクスポート
- **OBJ形式**
- **glTF形式**

編集したCityGMLの各地物をOBJやglTFとして書き出し、再度外部ツールで加工することもできます。

---
