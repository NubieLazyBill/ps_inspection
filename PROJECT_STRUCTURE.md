# 📁 Структура проекта "Осмотр ПС"

> Актуально на июнь 2026

## 🚀 Запуск приложения

| Файл | Что делает |
|------|------------|
| `SplashActivity.kt` | Экран-заставка |
| `MyApplication.kt` | Инициализация приложения |
| `MainActivity.kt` | Главная активность (навигация) |

---

## 📂 ui/fragments/ — Все экраны

### Основные экраны осмотра
| Файл | Что делает |
|------|------------|
| `HomeScreen.kt` | Главный экран (меню разделов) |
| `InspectionORU35.kt` | Ввод показаний ОРУ-35 кВ |
| `InspectionORU220.kt` | Ввод показаний ОРУ-220 кВ |
| `InspectionORU500.kt` | Ввод показаний ОРУ-500 кВ |
| `InspectionATG.kt` | Ввод показаний АТГ |
| `InspectionBuildings.kt` | Ввод показаний по зданиям |
| `ArchiveFragment.kt` | Архив осмотров (список) |
| `GraphsFragment.kt` | Графики / аналитика |

### Диалоги
| Файл | Что делает |
|------|------------|
| `CommentsDialogFragment.kt` | Диалог комментариев к оборудованию |
| `GlobalCommentsDialog.kt` | Все комментарии (глобально по разделам) |
| `MediaDialogFragment.kt` | Диалог с фото/медиа |
| `GlobalMediaDialog.kt` | Все фото (глобально по разделам) |
| `FullscreenPhotoDialog.kt` | Просмотр фото во весь экран |

### Утилиты (лежат в fragments по историческим причинам)
| Файл | Что делает |
|------|------------|
| `InputValidator.kt` | ⚠️ **ГРАНИЦЫ ЗНАЧЕНИЙ** (мин/макс для полей) |

---

## 📂 data/models/

| Файл | Что хранит |
|------|------------|
| `InspectionORU35Data.kt` | Показания ОРУ-35 |
| `InspectionORU220Data.kt` | Показания ОРУ-220 |
| `InspectionORU500Data.kt` | Показания ОРУ-500 |
| `InspectionATGData.kt` | Показания АТГ |
| `InspectionBuildingsData.kt` | Показания по зданиям |
| `InspectionArchiveData.kt` | Полный снимок осмотра |
| `ArchiveItem.kt` | Элемент списка в архиве |
| `Comment.kt` | Комментарий (текст, время, автор) |

---

## 📂 data/repositories/

| Файл | Что делает |
|------|------------|
| `InspectionArchiveManager.kt` | Сохранение/загрузка/удаление архивов |
| `CommentStorageManager.kt` | Хранение комментариев |
| `UserManager.kt` | Текущий пользователь |
| `AutoSaveManager.kt` | Автосохранение текущего осмотра |
| `ArchiveRepository.kt` | Доп. слой для работы с архивом |

---

## 📂 data/services/

| Файл | Что делает |
|------|------------|
| `GoogleSheetsService.kt` | Отправка/загрузка с Google Sheets |
| `ExcelExportService.kt` | Экспорт в Excel |

---

## 📂 data/utils/

| Файл | Что делает |
|------|------------|
| `MergeUtils.kt` | ⚠️ **ПЕРЕНОС ИЗ АРХИВА** (только пустые поля, переключатели `"○"`) |
| `ProgressCalculator.kt` | Расчёт процента заполнения разделов |
| `ArchiveExtensions.kt` | Функции-расширения для работы с архивом |
| `WeatherService.kt` | Получение погоды (температура) |

---

## 📂 ui/adapters/

| Файл | Что делает |
|------|------------|
| `ArchiveAdapter.kt` | Адаптер для списка архивов (RecyclerView) |

---

## 📂 viewmodel/

| Файл | Что делает |
|------|------------|
| `SharedInspectionViewModel.kt` | ⚠️ **ЦЕНТР ВСЕХ ДАННЫХ** — StateFlow для всех разделов, комментарии, фото, автосохранение |

---

## 🔗 Связи между файлами
SplashActivity → MainActivity → HomeScreen → остальные фрагменты

SharedInspectionViewModel (центр)
├── Inspection*Data (все модели)
├── CommentStorageManager
├── AutoSaveManager
├── GoogleSheetsService
└── WeatherService

MergeUtils (расширяет SharedInspectionViewModel)
└── перенос данных из архива (вызывается из ArchiveFragment)

ArchiveFragment
├── ArchiveAdapter
├── InspectionArchiveManager
├── ExcelExportService
├── GoogleSheetsService
├── CommentStorageManager
└── MergeUtils

InputValidator
└── проверка пределов (вызывается из фрагментов)

text

---

## ⚠️ ВАЖНЫЕ ПРАВИЛА (запомнить!)

| Правило | Где |
|---------|-----|
| 🔘 Переключатели хранятся как `"○"` / `"●"` | Во всех `*Data` классах |
| 📥 При переносе из архива — только если поле пустое или `"○"` | `MergeUtils.kt` |
| 💬 Комментарии и фото при переносе НЕ трогать | `MergeUtils.kt` |
| 📏 Пределы значений | `InputValidator.kt` (лежит в `ui/fragments`) |
| 💾 Автосохранение при каждом изменении | `SharedInspectionViewModel` |

---

## 🔍 Быстрый поиск (шпаргалка)

| Если нужно... | Иди в... |
|---------------|----------|
| Поменять пределы (мин/макс) | `ui/fragments/InputValidator.kt` |
| Поменять логику переноса из архива | `data/utils/MergeUtils.kt` |
| Добавить новое поле в осмотр | `data/models/*Data.kt` + `SharedInspectionViewModel` + фрагмент |
| Починить автосохранение | `SharedInspectionViewModel.autoSave()` |
| Починить отправку на сервер | `data/services/GoogleSheetsService.kt` |
| Починить экспорт в Excel | `data/services/ExcelExportService.kt` |
| Починить список архивов | `ui/fragments/ArchiveFragment.kt` |
| Найти переключатели `"○"` | Поиск по проекту: `"○"` |

---

## 📝 Примечания

1. **`InputValidator.kt` физически лежит в `ui/fragments`**, хотя логически относится к утилитам. Учтите при поиске.
2. **`WeatherService.kt`** лежит в `data/utils`, а не в `services` — тоже особенность структуры.