package com.example.util

data class HtmlTemplateItem(
    val id: String,
    val name: String,
    val description: String,
    val code: String
)

object HtmlTemplates {
    val defaultHtml = """<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>マイ HTML ページ</title>
    <style>
        body {
            font-family: 'Helvetica Neue', Arial, sans-serif;
            margin: 0;
            padding: 24px;
            background-color: #f8f9fa;
            color: #212529;
        }
        h1 {
            color: #2563eb;
        }
        .card {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }
    </style>
</head>
<body>
    <div class="card">
        <h1>Hello World! 👋</h1>
        <p>HTMLエディターへようこそ。コードを編集してリアルタイムで確認しましょう！</p>
    </div>
</body>
</html>"""

    val templates = listOf(
        HtmlTemplateItem(
            id = "blank_html5",
            name = "HTML5 基本テンプレート",
            description = "標準的なHTML5の基本骨格です。",
            code = """<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>新規ドキュメント</title>
</head>
<body>

    <h1>タイトル</h1>
    <p>ここにコンテンツを入力してください。</p>

</body>
</html>"""
        ),
        HtmlTemplateItem(
            id = "responsive_card",
            name = "モダンプロファイルカード",
            description = "CSS Flexboxとシャドウを使用した綺麗でモダングラデーションのカード。",
            code = """<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile Card</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            padding: 16px;
        }
        .profile-card {
            background: #ffffff;
            border-radius: 20px;
            padding: 32px 24px;
            width: 100%;
            max-width: 360px;
            text-align: center;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3);
        }
        .avatar {
            width: 88px;
            height: 88px;
            border-radius: 50%;
            background: linear-gradient(135deg, #3b82f6, #8b5cf6);
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
            color: white;
            font-size: 32px;
            font-weight: bold;
            box-shadow: 0 8px 16px rgba(59, 130, 246, 0.4);
        }
        .name {
            font-size: 22px;
            font-weight: 700;
            color: #0f172a;
            margin: 0 0 4px;
        }
        .role {
            font-size: 14px;
            color: #64748b;
            font-weight: 500;
            margin-bottom: 16px;
        }
        .bio {
            font-size: 14px;
            color: #334155;
            line-height: 1.6;
            margin-bottom: 24px;
        }
        .btn {
            display: inline-block;
            width: 100%;
            padding: 12px;
            background: #2563eb;
            color: white;
            text-decoration: none;
            border-radius: 10px;
            font-weight: 600;
            transition: background 0.2s;
        }
        .btn:active {
            background: #1d4ed8;
        }
    </style>
</head>
<body>
    <div class="profile-card">
        <div class="avatar">👨‍💻</div>
        <h2 class="name">山田 太郎</h2>
        <div class="role">Web Developer & Creator</div>
        <p class="bio">HTML/CSS/JSで使いやすく魅力的なWebサイトやアプリを制作しています。</p>
        <a href="#" class="btn" onclick="alert('メッセージを送信しました！')">コンタクトを取る</a>
    </div>
</body>
</html>"""
        ),
        HtmlTemplateItem(
            id = "contact_form",
            name = "スタイリッシュお問合せフォーム",
            description = "フォーム入力フィールド、ボタン、フォーカス効果付きフォームデザイン。",
            code = """<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>お問い合わせ</title>
    <style>
        body {
            font-family: sans-serif;
            background: #f1f5f9;
            padding: 24px 16px;
            margin: 0;
        }
        .form-container {
            max-width: 480px;
            margin: 0 auto;
            background: white;
            padding: 28px;
            border-radius: 16px;
            box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05);
        }
        h2 {
            margin-top: 0;
            color: #1e293b;
        }
        .form-group {
            margin-bottom: 18px;
        }
        label {
            display: block;
            margin-bottom: 6px;
            font-size: 14px;
            font-weight: 600;
            color: #475569;
        }
        input, textarea {
            width: 100%;
            padding: 12px;
            border: 1.5px solid #cbd5e1;
            border-radius: 8px;
            font-size: 15px;
            box-sizing: border-box;
            outline: none;
            transition: border-color 0.2s;
        }
        input:focus, textarea:focus {
            border-color: #3b82f6;
        }
        button {
            width: 100%;
            padding: 14px;
            background: #2563eb;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
        }
    </style>
</head>
<body>
    <div class="form-container">
        <h2>お問い合わせフォーム</h2>
        <form onsubmit="event.preventDefault(); alert('送信が完了しました！');">
            <div class="form-group">
                <label for="name">お名前</label>
                <input type="text" id="name" placeholder="例: 山田 花子" required>
            </div>
            <div class="form-group">
                <label for="email">メールアドレス</label>
                <input type="email" id="email" placeholder="example@email.com" required>
            </div>
            <div class="form-group">
                <label for="message">メッセージ</label>
                <textarea id="message" rows="4" placeholder="ご自由にご記入ください..." required></textarea>
            </div>
            <button type="submit">送信する</button>
        </form>
    </div>
</body>
</html>"""
        ),
        HtmlTemplateItem(
            id = "js_interactive_demo",
            name = "インタラクティブ JS デモ",
            description = "JavaScriptのロジック（カウンター＆カラーチェンジャー）が含まれたサンプル。",
            code = """<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Interactive JS Demo</title>
    <style>
        body {
            font-family: -apple-system, sans-serif;
            text-align: center;
            padding: 40px 20px;
            transition: background-color 0.3s;
            background-color: #ffffff;
        }
        .box {
            background: rgba(255, 255, 255, 0.9);
            display: inline-block;
            padding: 30px;
            border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
        }
        .count-display {
            font-size: 48px;
            font-weight: bold;
            color: #1e293b;
            margin: 20px 0;
        }
        .btn-group button {
            padding: 10px 20px;
            font-size: 18px;
            margin: 5px;
            border-radius: 8px;
            border: none;
            background: #3b82f6;
            color: white;
            font-weight: bold;
            cursor: pointer;
        }
        .color-btn {
            background: #10b981 !important;
            margin-top: 15px !important;
        }
    </style>
</head>
<body>
    <div class="box">
        <h2>リアルタイム JS デモ 🚀</h2>
        <div class="count-display" id="count">0</div>
        <div class="btn-group">
            <button onclick="decrement()">-1</button>
            <button onclick="reset()">リセット</button>
            <button onclick="increment()">+1</button>
        </div>
        <div>
            <button class="color-btn" onclick="changeBg()">背景色を変更 🎨</button>
        </div>
    </div>

    <script>
        let count = 0;
        const countEl = document.getElementById('count');
        const colors = ['#ffffff', '#fef3c7', '#dcfce7', '#e0e7ff', '#fce7f3'];

        function increment() {
            count++;
            countEl.textContent = count;
        }
        function decrement() {
            count--;
            countEl.textContent = count;
        }
        function reset() {
            count = 0;
            countEl.textContent = count;
        }
        function changeBg() {
            const randomColor = colors[Math.floor(Math.random() * colors.length)];
            document.body.style.backgroundColor = randomColor;
            console.log('背景色変更:', randomColor);
        }
    </script>
</body>
</html>"""
        )
    )
}
