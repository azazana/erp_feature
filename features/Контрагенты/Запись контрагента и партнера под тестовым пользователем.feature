﻿#language: ru

Функционал: Тестирование Записи партнера и контрагента

Как Тестовый пользователь
Я хочу протестировать запись контрагента и партнера
Чтобы Все было протестировано

Контекст:
	Дано Я открыл сеанс TestClient от имени "ТестовыйПользователь" с паролем "911" или подключаю уже существующий

Сценарий: Запись контрагента и партнера
	Когда я нажимаю на кнопку 'Клиенты'
	Тогда открылось окно 'Клиенты'
	И в поле 'СтрокаПоискаCRM' я ввожу текст 'СтройПром'
	И я перехожу к следующему реквизиту
	И в таблице "Список" я перехожу к строке:
	| Наименование  |
	| Аксайстройпром |
	И в таблице "Список" я выбираю текущую строку
	Тогда открылось окно 'Аксайстройпром (Партнер)'
	И я нажимаю на кнопку 'Записать'
	И В текущем окне я нажимаю кнопку командного интерфейса 'Контрагенты'
	# ВНИМАНИЕ: использование текущей строки без перехода к конкретной строке таблицы может быть ошибочным
	# Используйте исследователь формы или кнопки получения состояния формы\текущего элемента подменю "Форма" на закладке "Работа с UI" 
	# Ниже пример шага для выбора правильной строки таблицы
	# 	И в таблице "Список" я перехожу к строке:
	#   | Колонка1  | Колонка2  |
	#   | Значение1 | Значение2 |
	И в таблице "Список" я выбираю текущую строку
	Тогда открылось окно 'Аксайстройпром (Контрагент (юридическое или физическое лицо)'
	И я нажимаю на кнопку 'Записать и закрыть'
	И я жду закрытия окна 'Аксайстройпром (Контрагент (юридическое или физическое лицо)' в течение 20 секунд
	Тогда открылось окно 'Аксайстройпром (Партнер)'
	И В текущем окне я нажимаю кнопку командного интерфейса 'Основное'
	И я нажимаю на кнопку 'Записать и закрыть'
	И я жду закрытия окна 'Аксайстройпром (Партнер)' в течение 20 секунд
