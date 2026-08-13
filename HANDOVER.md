# プロジェクト引き継ぎ資料 (HANDOVER.md)

本ドキュメントは、**HTML Editor & Live Preview App for Android** の開発経緯、技術スタック、ディレクトリ構造、GitHub Actions CI/CD 設定、および次回セッション再開時の標準運用手順をまとめた完全引き継ぎ資料です。

---

## 1. プロジェクト概要

* **アプリ名**: HtmlEditorApp (HTML Editor & Live Preview)
* **開発言語**: Kotlin 100%
* **UI フレームワーク**: Jetpack Compose (Material Design 3)
* **目的**: スマホ上で直感的に HTML / CSS / JavaScript を編集・即時リアルタイムプレビューできる Android アプリケーション。
* **主要機能**:
  1. **リアルタイム HTML プレビュー Engine**: 編集したコードを即時反映（モバイル / デスクトップ表示切り替え可能）。
  2. **スマートコードエディター**:
     * 行番号表示・アクティブ行ハイライト
     * 検索・置換（Search & Replace）
     * 常用 HTML タグ（`<div>`, `<p>`, `<a>`, `<style>`, `<script>` 等）のワンタップ挿入ツールバー
     * コード自動フォーマット（整形）機能
     * Undo / Redo（元に戻す / やり直す）
  3. **Android 連携 & ストレージ機能**:
     * Android 標準 Share Sheet（共有メニュー）経由で他アプリから HTML ファイルを直接受け取って編集
     * ローカルストレージ（`content://`, `file://`）のHTML直接読み込み & 上書き保存
     * プリセットテンプレート（ポートフォリオ、LP、ブログ等）からの新規作成
     * 最近開いたファイルの履歴管理（Room データベースによるローカル永続化）

---

## 2. 技術スタック & 依存ライブラリ

* **Android Gradle Plugin (AGP)**: 8.8.2
* **Gradle Wrapper**: 9.3.1 (`gradle/wrapper/gradle-wrapper.properties`)
* **JDK バージョン**: Java 17 / Temurin
* **Target / Compile SDK**: Android 16 (API 36) / Android 15 (API 35)
* **Min SDK**: Android 7.0 (API 24)
* **主要ライブラリ**:
  * `androidx.compose.ui`: UI レイアウト設計
  * `androidx.compose.material3`: Material Design 3 デザインシステム
  * `androidx.room`: 最近開いたファイル等のローカル DB 永続化
  * `kotlinx.coroutines` / `Flow`: 非同期状態管理 & リアルタイム更新

---

## 3. ディレクトリ構造

```text
code-editor-android/
│
├── .github/
│   └── workflows/
│       └── android.yml            # GitHub Actions 自動APKビルド定義ファイル
│
├── app/                           # メインアプリケーションモジュール
│   ├── build.gradle.kts           # アプリレベルのビルド設定 & 署名設定
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml # インテントフィルター (Share Sheet/File Open) 定義
│           ├── java/com/example/  # Jetpack Compose UI, ViewModel, Room DB 実装
│           └── res/               # アプリ描画・文字列リソース (strings.xml, drawables)
│
├── gradle/
│   ├── libs.versions.toml         # Version Catalog (依存ライブラリバージョン一元管理)
│   └── wrapper/
│       ├── gradle-wrapper.properties  # Gradle 9.3.1 指定設定
│       └── gradle-wrapper.jar
│
├── build.gradle.kts               # ルートレベル Gradle スクリプト
├── settings.gradle.kts            # ルートプロジェクト名・モジュール定義
├── gradlew                        # Linux/Mac 用 Gradle Wrapper 起動スクリプト
├── gradlew.bat                    # Windows 用 Gradle Wrapper 起動スクリプト
├── README.md                      # プロジェクト説明ファイル
├── HANDOVER.md                    # 【本資料】セッション引き継ぎドキュメント
├── BUILD_APK_WORKFLOW.yml         # GitHub Actions 用予備ワークフローファイル
└── GRADLE_WRAPPER_PROPERTIES.txt  # gradle-wrapper.properties のテキストバックアップ
```

---

## 4. 解決済み課題 & 設定の重要ポイント

次回セッションで同様のビルドエラーや認識違いを発生させないための重要な解決履歴です：

### ① Gradle 9.3.1 & Gradle Wrapper の整合性
* **課題**: AGP 8.8.2 / 9.1.1 互換および KSP (Kotlin Symbol Processing) の依存関係により、GitHub Actions 上で Gradle バージョンミスマッチエラーが発生した。
* **解決策**: `gradle/wrapper/gradle-wrapper.properties` にて **Gradle 9.3.1** を明示指定。GitHub Actions でも `gradle` 直呼び出しではなく `./gradlew assembleDebug` を使用するように変更。

### ② `debug.keystore` 署名問題のフォールバック化
* **課題**: `app/build.gradle.kts` で `debug.keystore` の存在が必須となっており、GitHub Actions などのクリーン環境でビルド失敗の恐れがあった。
* **解決策**: `debug.keystore` ファイルが存在する場合のみカスタムキーを読み込み、存在しない場合は Android Gradle Plugin 標準の自動生成デバッグ署名にフォールバックする判定コードを実装済み。

---

## 5. GitHub 共有・コミット運用手順

このリポジトリは初期化済みで、GitHub の remote は SSH に設定済みです。

* **Repository**: `Minashin1120/code-editor-android`
* **作業ブランチ**: `main`
* **Remote**: `git@github.com:Minashin1120/code-editor-android.git`
* **SSH 認証鍵**: `%USERPROFILE%\.ssh\id_ed25519`
* **コミット署名**: SSH 署名をデフォルトで有効化済み

通常の変更・コミット・push は以下で行います。

```powershell
cd C:\Users\shintaro\Downloads\html-editor
git status
git add .
git commit -m "変更内容"
git push
```

GitHub 側で公開鍵を **Authentication** と **Signing** の両方に登録しておく必要があります。コミット作成者のメールアドレスは、GitHub で認証済みのメールアドレスを使用してください。コミットページで `Verified` と表示されることを確認します。

リモートに先行コミットがある場合は、まず取得して rebase します。競合を解消してから push してください。

```powershell
git fetch origin
git rebase origin/main
git add <競合を解消したファイル>
git rebase --continue
git push
```

履歴を壊す可能性があるため、通常運用では `--force` を使用しません。

### 別の PC に変更した場合の Git セットアップ

新しい Windows PC では、Git、SSH 認証、コミット署名を次の順で設定します。秘密鍵は安全な方法で移行し、紛失・漏えいした場合は GitHub から該当鍵を削除して新しい鍵を作成してください。

1. Git をインストールします。

```powershell
winget install --id Git.Git -e --source winget
```

2. 既存の SSH 鍵を移行するか、新しい鍵を作成します。新規作成する場合は、Git Bash または PowerShell で実行します。

```powershell
ssh-keygen -t ed25519 -C "GitHubに登録済みのメールアドレス"
```

既定の保存先は `C:\Users\<ユーザー名>\.ssh\id_ed25519` です。秘密鍵（拡張子なし）は共有・公開せず、公開鍵だけを GitHub に登録します。

3. SSH agent に鍵を登録します。環境によっては PowerShell を管理者として実行します。

```powershell
Get-Service ssh-agent | Set-Service -StartupType Automatic
Start-Service ssh-agent
ssh-add "$env:USERPROFILE\.ssh\id_ed25519"
```

4. GitHub の **Settings → SSH and GPG keys** に `id_ed25519.pub` の内容を登録します。同じ公開鍵を **Authentication** と **Signing** の2用途で登録します。

```powershell
Get-Content "$env:USERPROFILE\.ssh\id_ed25519.pub" | Set-Clipboard
```

5. SSH 接続を確認し、SSH URL でリポジトリを clone します。

```powershell
ssh -T git@github.com
git clone git@github.com:Minashin1120/code-editor-android.git
cd code-editor-android
```

既存の作業フォルダをコピーした場合は、remote を確認し、必要なら次で修正します。

```powershell
git remote set-url origin git@github.com:Minashin1120/code-editor-android.git
```

6. Git の作成者情報と SSH 署名を設定します。メールアドレスは GitHub で認証済みのものを使います。

```powershell
git config --global user.name "Minashin1120"
git config --global user.email "GitHubで認証済みのメールアドレス"
git config --global gpg.format ssh
git config --global user.signingkey "$env:USERPROFILE/.ssh/id_ed25519.pub"
git config --global commit.gpgsign true
```

7. 設定と署名を確認します。

```powershell
git config --show-origin --get-regexp "^(user\.|gpg\.|commit\.gpgsign|remote\.)"
```

次回の実際の変更をコミットした後、GitHub 上で `Verified` 表示を確認します。秘密鍵のパスフレーズや内容を AI、ログ、リポジトリへ渡してはいけません。

---

## 6. GitHub Actions による自動 APK ビルド仕組み

1. `main` への `git push` が成功すると、GitHub 上の `.github/workflows/android.yml` が自動発火します。
2. GitHub Actions 上で JDK 17 と Gradle 8.10.2 が設定され、`gradle assembleDebug --stacktrace` が実行されます。
3. ビルド成功後、GitHub リポジトリの **「Actions」** タブ ➔ 該当ワークフロー実行結果の一番下にある **Artifacts** に **`html-editor-app-debug-apk`** が生成され、スマホにインストール可能な `.apk` ファイルがダウンロードできます。

---

## 7. 次回チャットセッション再開時のプロンプト例

チャットセッションを初期化・新規開局した際は、以下の文面を新しい AI に入力することで、本ドキュメントを読み込んで即座に開発を再開できます：

> **「ルートディレクトリにある `HANDOVER.md` および `README.md` を確認し、この HTML Editor アプリの開発状況を把握した上で指示に従ってください。」**
