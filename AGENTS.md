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

## Git と自動 push

依頼された変更の実装と検証が完了したら、AI エージェントは自動的に次を実行します。

```powershell
git diff --check
git add <変更ファイル>
git diff --cached --check
git commit -m "変更内容"
git push
```

push 前に、変更対象とコミット内容を確認します。remote は SSH URL を使用し、既存の未コミット変更を含めません。

リモートに先行コミットがある場合は `git fetch origin` 後に通常の rebase を行います。競合時は内容を確認して統合します。`git push --force`、履歴の書き換え、`main` への直接 push は明示的な依頼なしに実行しません。

## 秘密情報と実装上の注意

- API キー、keystore、パスワード、`.env` などの秘密情報をコミットしません。
- `app/build.gradle.kts` の debug signing は、カスタム keystore がない場合に標準設定へフォールバックさせます。
- Android の UI やデータ保存仕様を変更する場合は、関連する ViewModel、Repository、Room DAO、Manifest も確認します。

## 完了前チェック

```powershell
git diff --check
git status
```

最終報告には、変更内容、実行したテスト、未解決事項、push したコミットを記載します。
