# AGENTS.md

このファイルは、`code-editor-android` リポジトリを扱う AI エージェント向けの運用ガイドです。

## 作業開始時の確認

作業前にリポジトリのルートで次を確認します。

```powershell
git status
git branch --show-current
git remote -v
git log -3 --oneline
```

既存の未コミット変更は削除・上書きしません。作業対象が重なる場合は、変更内容を確認してから編集します。

## ビルドとテスト

GitHub Actions は `.github/workflows/android.yml` が管理します。JDK 17、Gradle 9.3.1 を使用し、`gradle assembleDebug --stacktrace` を実行します。

ローカルで利用可能な場合は、Gradle Wrapper で次を実行します。

```powershell
gradle assembleDebug --stacktrace
gradle test
```

workflow や Gradle 設定を変更した場合は、変更理由と検証結果をコミットメッセージまたは引き継ぎ資料に残します。

### APK ビルドのスキップ

アプリ本体・バージョン・Gradle に関係しない変更だけを push する場合は、コミットメッセージに `[skip build]` を付ける。`android.yml` はこの印がある push では `assembleDebug` と GitHub Release を実行しない。`[skip ci]` も同じ扱い。

スキップしてよい例:

- `AGENTS.md` や README だけの修正
- コメントやドキュメントだけの修正
- APK も版番号も変えない運用メモ

スキップしてはいけない例:

- `app/` のコードやリソースを変えた
- `versionName` / `versionCode` / `version.json` を上げた
- ユーザーが APK・リリース・更新確認を求めている

手動実行（`workflow_dispatch`）と pull request では `[skip build]` は効かない。今走っているジョブを後から止める用途ではない。

ドキュメントだけの変更を push するときは `[skip build]` を付け、不要な APK ビルドを起こさない。

## 更新確認とリリース

アプリ内の「更新を確認」は、GitHub 上の公開情報だけを見る。配布の正本は GitHub Release であり、Actions の成果物だけでは更新として検出されない。

確認先:

- `version.json`（`main` の raw と Contents API）
- GitHub Releases（`/releases/latest` とリリース一覧）

比較は `app/build.gradle.kts` の `versionCode` を優先し、無ければ `versionName` を使う。インストール済みと同じ版なら「最新」と表示するのが正しい。Release が無い、または公開版が端末と同じなら、更新は出ない。不具合ではない。

### 更新として出すときの必須手順

利用者に届く変更を push して更新として出すなら、機能変更と版上げを同じコミット（または同じ push）で行う。機能だけ先に push して後から版だけ上げると、CI がフルビルドを二度走らせる。

1. `app/build.gradle.kts` の `versionName` と `versionCode` を両方上げる
2. `version.json` に同じ値を書く。片方だけ変えない
3. `[skip build]` は付けない。CI に APK をビルドさせ、タグ `v{versionName}` の Release を作らせる

やってはいけないこと:

- 機能だけ push して版を据え置く。既存の `v{versionName}` Release の APK は上書きされるが、同じ版が入っている端末は更新を検出しない
- 版を上げずに「更新が無い」のを不具合として追いかけて、後から版だけ上げて二度目のビルドを起こす
- 依頼なしに `git tag` や `git push --tags` をする

公開手順:

- `main` / `master` への push で `.github/workflows/android.yml` が debug APK をビルドし、タグ `v{versionName}` の GitHub Release を作成または更新する。同じ `versionName` のまま push すると、そのタグと Release 上の APK を上書きする。この上書きだけでは更新通知は出ない。
- ユーザーに新しい更新として通知するには、`versionName` と `versionCode` を上げ、同じ値を `version.json` にも書く。片方だけ変えない。
- エージェントは依頼なしに `git tag` や `git push --tags` をしない。リリースタグは CI が作る。
- `v*` タグの push と workflow の手動実行は `.github/workflows/release.yml` が扱う。通常の公開は android.yml 側に任せる。
- ユーザーが push を頼んでいて、変更がアプリ利用者に届くものなら、版上げを省略しない。ドキュメントだけの変更なら版は上げず `[skip build]` を使う。

## Git

依頼された変更の実装と検証が完了したら、現在のブランチでローカルコミットまで行います。

```powershell
git diff --check
git add <変更ファイル>
git diff --cached --check
git commit -m "変更内容"
```

変更対象とコミット内容を確認します。既存の未コミット変更は含めません。APK が不要な変更を push するときは、コミットメッセージに `[skip build]` を含めます。

`git push` は、ユーザーが明示的に依頼したときだけ実行します。`main` だから控える、別ブランチを切って代わりに push する、といった読み替えはしません。`git push --force` と履歴の書き換えは、明示的な依頼なしに実行しません。

リモートに先行コミットがある作業では `git fetch origin` 後に通常の rebase を行います。競合時は内容を確認して統合します。remote は SSH URL を使用します。

## 秘密情報と実装上の注意

- API キー、keystore、パスワード、`.env` などの秘密情報をコミットしません。
- `app/build.gradle.kts` の debug signing は、リポジトリ直下の `debug.keystore` を使います。CI とローカルで署名を揃えるため、このファイルはコミットします。upload 用の `*.jks` はコミットしません。
- Android の UI やデータ保存仕様を変更する場合は、関連する ViewModel、Repository、Room DAO、Manifest も確認します。

## 完了前チェック

```powershell
git diff --check
git status
```

最終報告には、変更内容、実行したテスト、未解決事項、作成したコミットを記載します。push した場合のみ、その事実も書きます。
