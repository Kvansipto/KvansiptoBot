-- Шаг 1: Удаляем данные с id 1 и 2
DELETE FROM exercise WHERE id in ('1', '2');

-- Шаг 2: Убираем внешнюю связь
ALTER TABLE exercise_result DROP CONSTRAINT exercise_result_exercise_id_fkey;
ALTER TABLE exercise_result DROP CONSTRAINT exercise_result_user_id_fkey;

-- Шаг 3: Приводим тип данных exercise_id и user_id в таблице exercise_result к BIGINT
ALTER TABLE exercise_result
    ALTER COLUMN exercise_id TYPE BIGINT USING exercise_id::BIGINT,
    ALTER COLUMN id TYPE BIGINT USING id::BIGINT,
    ALTER COLUMN user_id TYPE BIGINT USING user_id::BIGINT;

-- Шаг 4: Приводим тип данных id в таблицах exercise и users к BIGINT
ALTER TABLE users
    ALTER COLUMN id TYPE BIGINT USING id::BIGINT;
ALTER TABLE exercise
    ALTER COLUMN id TYPE BIGINT USING id::BIGINT;

-- Шаг 5: Восстанавливаем внешние связи
ALTER TABLE exercise_result
    ADD CONSTRAINT exercise_result_exercise_id_fkey FOREIGN KEY (exercise_id) REFERENCES exercise(id),
    ADD CONSTRAINT exercise_result_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);

-- Шаг 6: Вставляем новые данные
INSERT INTO exercise (id, description, image_url, muscle_group, name, video_url)
VALUES (3, null, null, 'CHEST', 'Жим лежа', null),
       (4, null, null, 'CHEST', 'Жим гантелей на скамье под наклоном', null),
       (5, null, null, 'CHEST', 'Разведение гантелей', null),
       (6, null, null, 'ARMS', 'Разгибания на трицепс в блоке', null),
       (7, null, null, 'BACK', 'Тяга верхнего блока', null),
       (8, null, null, 'BACK', 'Тяга горизонтального блока', null),
       (9, null, null, 'BACK', 'Тяга гантелей в наклоне', null),
       (10, null, null, 'ARMS', 'Подъемы на бицепс со штангой', null),
       (11, null, null, 'ARMS', 'Подъемы на бицепс с гантелями', null),
       (12, null, null, 'ARMS', 'Французский жим с гантелями', null),
       (13, null, null, 'LEGS', 'Приседания со штангой', null),
       (14, null, null, 'LEGS', 'Жим ногами', null),
       (15, null, null, 'LEGS', 'Выпады с гантелями', null),
       (16, null, null, 'LEGS', 'Разгибания ног в тренажере', null),
       (17, null, null, 'LEGS', 'Сгибания ног в тренажере', null),
       (18, null, null, 'SHOULDERS', 'Жим гантелей сидя', null),
       (19, null, null, 'SHOULDERS', 'Подъемы гантелей в стороны', null),
       (20, null, null, 'CORE', 'Скручивания на пресс', null),
       (21, null, null, 'SHOULDERS', 'Разведение гантелей в наклоне', 'https://youtu.be/ttvfGg9d76c?si=_xc3EXWeZ7G65H4h'),
       (22, null, null, 'SHOULDERS', 'Жим Арнольда', null),
       (23, null, null, 'SHOULDERS', 'Армейский жим', null),
       (24, null, null, 'CORE', 'Подъемы ног в висе', null)
ON CONFLICT (id) DO NOTHING;
