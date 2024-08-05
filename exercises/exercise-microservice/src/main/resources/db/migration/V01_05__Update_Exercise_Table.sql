UPDATE public.exercise
SET
    description = updates.description,
    image_url = updates.image_url,
    muscle_group = updates.muscle_group,
    name = updates.name,
    video_url = updates.video_url
FROM (
         VALUES
             (6, 'Разгибания на трицепс в блоке', NULL, 'ARMS', 'Triceps extension', NULL),
             (7, 'Тяга верхнего блока', NULL, 'BACK', 'Upper block pulls', NULL),
             (8, 'Тяга горизонтального блока', NULL, 'BACK', 'Horizontal block pulls', NULL),
             (9, 'Тяга гантелей в наклоне', NULL, 'BACK', 'Bent-over dumbbell row', NULL),
             (10, 'Подъемы на бицепс со штангой', NULL, 'ARMS', 'Barbell bicep curls', NULL),
             (11, 'Подъемы на бицепс с гантелями', NULL, 'ARMS', 'Dumbbell bicep curls', NULL),
             (12, 'Французский жим с гантелями', NULL, 'ARMS', 'Dumbbell French press', NULL),
             (13, 'Приседания со штангой', NULL, 'LEGS', 'Barbell squats', NULL),
             (14, 'Жим ногами', NULL, 'LEGS', 'Leg press', NULL),
             (15, 'Выпады с гантелями', NULL, 'LEGS', 'Dumbbell lunges', NULL),
             (16, 'Разгибания ног в тренажере', NULL, 'LEGS', 'Leg extensions on the machine', NULL),
             (17, 'Сгибания ног в тренажере', NULL, 'LEGS', 'Leg curls on the machine', NULL),
             (18, 'Жим гантелей сидя', NULL, 'SHOULDERS', 'Dumbbell press', NULL),
             (19, 'Подъемы гантелей в стороны', NULL, 'SHOULDERS', 'Lateral dumbbell raises', NULL),
             (20, 'Скручивания на пресс', NULL, 'CORE', 'Ab crunches', NULL),
             (21, 'Разведение гантелей в наклоне', NULL, 'SHOULDERS', 'Bent-over dumbbell flyes', 'https://youtu.be/ttvfGg9d76c?si=_xc3EXWeZ7G65H4h'),
             (22, 'Жим Арнольда', NULL, 'SHOULDERS', 'Arnold press', NULL),
             (23, 'Армейский жим', NULL, 'SHOULDERS', 'Military press', NULL),
             (24, 'Подъемы ног в висе', NULL, 'CORE', 'Hanging leg raises', NULL),
             (4, 'Жим гантелей', NULL, 'CHEST', 'Dumbbell bench press', NULL),
             (5, 'Разведение гантелей', NULL, 'CHEST', 'Dumbbell flyes', NULL),
             (3, 'Жим штанги лежа', NULL, 'CHEST', 'Barbell bench press', NULL)
     ) AS updates(id, description, image_url, muscle_group, name, video_url)
WHERE public.exercise.id = updates.id;
