insert into public.exercise (id, description, image_url, muscle_group, name, video_url)
values (1, 'asds', 'https://i.pinimg.com/236x/2a/f5/3d/2af53d8f1be483dd0e05b7b18142c33c.jpg', 'BACK', 'подтягивания',
        'https://youtu.be/6U9HYTyTlVw?si=xG_zLVRVS0GOXrrK'),
       (2, 'asds', 'https://i.pinimg.com/236x/2a/f5/3d/2af53d8f1be483dd0e05b7b18142c33c.jpg', 'CHEST', 'жим',
        'https://youtu.be/6U9HYTyTlVw?si=xG_zLVRVS0GOXrrK')
ON CONFLICT (id) DO NOTHING;