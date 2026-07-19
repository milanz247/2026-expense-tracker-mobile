# Expense Tracker — Mobile

Native Android client for a personal finance app, built in Kotlin with Jetpack Compose.

Part of a three-repo project:
- **expense-tracker-mobile** (this repo)
- [expense-tracker-backend](https://github.com/milanz247/2026-expense-tracker-backend) — Go REST API
- [expense-tracker-frontend](https://github.com/milanz247/2026-expense-tracker-frontend) — Next.js web client

## Features

- JWT authentication against the backend API (`AuthInterceptor`)
- Session persistence via Jetpack DataStore
- Compose Navigation between screens
- Built-in Gemini AI helper for financial Q&A on top of your own data

## Stack

Kotlin · Jetpack Compose · Retrofit/OkHttp networking · DataStore · Gemini API

## Getting started

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open the project in Android Studio and let it sync Gradle
2. Copy `.env.example` to `.env` and set `GEMINI_API_KEY`
3. Point the network client at a running [expense-tracker-backend](https://github.com/milanz247/2026-expense-tracker-backend) instance
4. Run on an emulator or physical device
