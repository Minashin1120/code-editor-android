# AGENTS.md

このファイルは、`code-editor-android` リポジトリを扱う AI エージェント向けの運用ガイドです。

## リポジトリ概要

- Android アプリ「HTML Editor & Live Preview App」
- Kotlin / Jetpack Compose / Material 3
- Room による最近のファイル履歴管理
- GitHub Actions で Debug APK をビルド
- GitHub: `Minashin1120/code-editor-android`
- デフォルトブランチ: `main`

## 作業開始時の確認

作業前に、必ずリポジトリのルートで次を確認します。

```powershell
git status
git branch --show-current
git remote -v
git log -3 --oneline
```

既存の未コミット変更は、依頼された変更と無関係でも削除・上書きしません。作業対象が重なる場合は、変更内容を確認してから編集します。

## ビルドとテスト

Windows では Gradle Wrapper を使用します。

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

GitHub Actions は `.github/workflows/android.yml` が管理します。JDK 17、Gradle 8.10.2 を使用し、`gradle assembleDebug --stacktrace` を実行します。workflow や Gradle 設定を変更した場合は、変更理由と検証結果をコミットメッセージまたは引き継ぎ資料に残します。

## Git とコミット

remote は HTTPS に変更せず、SSH URL を使用します。

```text
git@github.com:Minashin1120/code-editor-android.git
```

通常の変更手順：

```powershell
git add <変更ファイル>
git diff --cached --check
git commit -m "変更内容"
git push
```

コミットは SSH 署名付きにし、GitHub 上で `Verified` になる状態を維持します。署名用の秘密鍵をリポジトリへ追加したり、ログやチャットに表示したりしてはいけません。

確認コマンド：

```powershell
git config --local --get-regexp "^(user\.|gpg\.|commit\.gpgsign|remote\.)"
git cat-file commit HEAD | Select-String "gpgsig"
```

リモートに先行コミットがある場合は、通常の push 前に次を実行します。

```powershell
git fetch origin
git rebase origin/main
```

競合時は内容を確認して統合し、`git add <ファイル>`、`git rebase --continue` の順に進めます。`git rebase --abort` は、rebase 開始前へ戻す必要がある場合だけ使用します。履歴を壊す可能性があるため、`git push --force` は明示的な依頼なしに実行しません。

## 重要な実装上の注意

- `app/build.gradle.kts` の `debug.keystore` は存在する場合だけ使用し、存在しない場合は Android Gradle Plugin の標準デバッグ署名へフォールバックします。
- 秘密情報、API キー、keystore、パスワードをコミットしません。`.env` や署名用ファイルの扱いを変更する場合は、まず `.gitignore` と `.env.example` を確認します。
- Android の既存 UI やデータ保存仕様を変更する場合は、関連する ViewModel、Repository、Room DAO、Manifest も確認します。
- 画像や UI の変更では、既存のリソース命名規則とテーマ定義を再利用します。

## 完了前チェック

```powershell
git diff --check
git status
```

変更内容、実行したテスト、未解決事項を最終報告に記載します。依頼されていないファイル変更や破壊的な Git 操作は行いません。
