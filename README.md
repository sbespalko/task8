# Запуск

## Запускаем main() в:
com.sbt.hakaton.task8.starter.Task8Application

## Отправляем GET запрос: 
http://localhost:8080/start/{threshold}
* threshold - сколько запросов будет в секунду (на малых числах рабоать не будет)

## Остановка: 
http://localhost:8080/stop
#### Выведет статистическую инфу: 
* кол-во сгенерённых объектов
* длительность процесса, в сек
* tps