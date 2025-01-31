﻿#language: ru

Функционал: Проведение заказа клиента

Как Тестовый пользователь
Я хочу Провести и заполнить заказ клинета
Чтобы протестировать его проведение

Контекст:
	Дано Я открыл сеанс TestClient от имени "ТестовыйПользователь" с паролем "911" или подключаю уже существующий

Сценарий: Проведение заказа клиента
	Когда я нажимаю на кнопку 'Заказы клиентов'
	Тогда открылось окно "Заказы клиентов"
	И в таблице "Список" я нажимаю на кнопку с именем 'СписокСоздать'
	Тогда открылось окно 'Заказ клиента (создание)'
	И я нажимаю кнопку выбора у поля с именем "Организация"
	# Возможны проблемы при использовании точного выбора, т.к. подобный "быстрый выбор" актуален только для текущей базы и вашего пользователя.
	# подробнее в FAQ - Можно ли использовать быстрый выбор из списков 1С в полях ссылочных реквизитов
	И из выпадающего списка с именем "Организация" я выбираю по строке 'КАМИ-ГРУПП'
	Тогда открылось окно 'Заказ клиента (создание) *'
	И я перехожу к следующему реквизиту
	# Возможны проблемы при использовании точного выбора, т.к. подобный "быстрый выбор" актуален только для текущей базы и вашего пользователя.
	# подробнее в FAQ - Можно ли использовать быстрый выбор из списков 1С в полях ссылочных реквизитов
	И из выпадающего списка с именем "ПартнерБезКЛ" я выбираю по строке '10 Бар ооо'
	И я перехожу к следующему реквизиту
	# Возможны проблемы при использовании точного выбора, т.к. подобный "быстрый выбор" актуален только для текущей базы и вашего пользователя.
	# подробнее в FAQ - Можно ли использовать быстрый выбор из списков 1С в полях ссылочных реквизитов
	И из выпадающего списка "Контрагент" я выбираю по строке '10 бар ооо'
	И я перехожу к следующему реквизиту
	# Возможны проблемы при использовании точного выбора, т.к. подобный "быстрый выбор" актуален только для текущей базы и вашего пользователя.
	# подробнее в FAQ - Можно ли использовать быстрый выбор из списков 1С в полях ссылочных реквизитов
	И из выпадающего списка "Склад" я выбираю по строке 'Ступино'
	И я перехожу к следующему реквизиту
	# Возможны проблемы при использовании точного выбора, т.к. подобный "быстрый выбор" актуален только для текущей базы и вашего пользователя.
	# подробнее в FAQ - Можно ли использовать быстрый выбор из списков 1С в полях ссылочных реквизитов
	И из выпадающего списка "Соглашение" я выбираю по строке '100% руб'
	И я перехожу к следующему реквизиту
	И я перехожу к закладке "Товары"
	И в таблице "Товары" я нажимаю на кнопку с именем 'ТоварыДобавить'
	# Возможны проблемы при использовании точного выбора, т.к. подобный "быстрый выбор" актуален только для текущей базы и вашего пользователя.
	# подробнее в FAQ - Можно ли использовать быстрый выбор из списков 1С в полях ссылочных реквизитов
	И в таблице "Товары" из выпадающего списка "Номенклатура" я выбираю по строке 'КА 437286'
	# ВНИМАНИЕ: использование текущей строки без перехода к конкретной строке таблицы может быть ошибочным
	# Используйте исследователь формы или кнопки получения состояния формы\текущего элемента подменю "Форма" на закладке "Работа с UI" 
	# Ниже пример шага для выбора правильной строки таблицы
	# 	И в таблице "Товары" я перехожу к строке:
	#   | Колонка1  | Колонка2  |
	#   | Значение1 | Значение2 |
	И в таблице "Товары" я активизирую поле "Вид цены"
	И в таблице "Товары" из выпадающего списка "Вид цены" я выбираю точное значение 'Розничная цена'
	И в таблице "Товары" я активизирую поле "Ставка НДС"
	И в таблице "Товары" из выпадающего списка "Ставка НДС" я выбираю точное значение '20%'
	И я перехожу к закладке "Доставка"
	И в поле 'Срок поставки' я ввожу текст '3'
	И я нажимаю на кнопку 'Провести и закрыть'
	И я жду закрытия окна 'Заказ клиента (создание)' в течение 20 секунд

