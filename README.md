# MediaToggle

XREAL Beam Pro のボタンでメディア操作をするためのヘッドレスツールです。
本体に画面はなく、ボタン操作を受けて再生/停止や曲送りなどのメディアキーを送ります。

## 概要

* アプリ名: MediaToggle (`dev.sasnews.mediatoggle`)
* 目的: XREAL Beam Pro のボタンでメディア制御をするヘッドレスツール
* 画面を持たない Activity がキーイベントを受けて AudioManager 経由でメディアキーを送ります

## 割り当て方法

1. 本アプリをインストールします
2. XREAL 設定を開きます
3. ボタン割り当てで本アプリ (MediaToggle) をボタンへ割り当てます
4. ボタンを押して動作を確認します

## 操作

タップ回数で動作が変わります。判定の時間幅は約 300ms です (設定で変更できます)。

| 操作 | 既定の動作 | 内容 |
| --- | --- | --- |
| シングル | PLAY_PAUSE (再生/停止) | 1回押しで再生と停止を切り替えます |
| ダブル | NEXT (次の曲) | 2回押しで次の曲に進みます |
| トリプル | PREVIOUS (前の曲) | 3回押しで前の曲に戻ります |

* シングル、ダブル、トリプルの割り当ては設定画面で変更できます
* タップ判定の時間幅の既定値は 300ms で、200ms から 500ms の範囲で変えられます

## 設定の変更

1. ランチャーで MediaToggle のアプリアイコンを長押しします
2. 長押しメニューから「設定」を選びます
3. シングル、ダブル、トリプルの動作とタップ判定時間 (200〜500ms) を変えて保存します

注意: App Shortcuts は API 25+ 必須です。API 25 未満の端末では長押しメニューが出ません。

## インストール

* GitHub Releases にある CI リリース APK を入手してインストールします
* `v*` タグが push されると release.yml が動いて署名済み APK が Releases に上がります

## ローカルビルド

```sh
./gradlew assembleDebug
```

APK は `build/outputs/apk/debug/` に出ます (例: `MediaToggle-debug.apk`)。

## リリース方法

リリースは `v*` タグ push で行います。

```sh
git tag vX.Y.Z
git push origin vX.Y.Z
```

タグを push すると `.github/workflows/release.yml` が走り、署名済みリリース APK をビルドして GitHub Releases に添付します。

## keystore 生成と Secrets 設定

リリース署名には keystore が要ります。例:

```sh
keytool -genkeypair -v -keystore release.jks -alias mediatoggle -keyalg RSA -keysize 2048 -validity 10000
```

GitHub Secrets に次の3つを登録します。

* `ANDROID_KEYSTORE`: keystore ファイルの base64
* `KEYSTORE_PASSWORD`: keystore のパスワード
* `KEY_ALIAS`: 署名に使うエイリアス

base64 化と登録の例:

```sh
base64 -w0 release.jks > release.jks.b64
gh secret set ANDROID_KEYSTORE < release.jks.b64
gh secret set KEYSTORE_PASSWORD
gh secret set KEY_ALIAS
```

警告: keystore ファイル (*.jks) とパスワードはリポジトリに絶対コミットしないでください。`.gitignore` で除外しています。

## 既知の制限

* 画面OFF のときは反応しません
* YouTube 全画面再生中は PiP への遷移 (PiP ホーム) になることがあり、期待どおりの送り操作になりません
* マルチタップ判定はプロセス生存依存です。プロセスが殺されるとダブルやトリプルの連続判定が切れることがあります
* App Shortcuts を使う設定ショートカットは API 25+ が要ります
