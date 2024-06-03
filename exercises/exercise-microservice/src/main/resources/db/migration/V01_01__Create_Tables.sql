create table if not exists public.users
(
    id            varchar(255) not null
        primary key,
    first_name    varchar(255),
    last_name     varchar(255),
    registered_at timestamp(6) without time zone,
    user_name     varchar(255)
);

create table if not exists public.exercise
(
    id           varchar(255) not null
        primary key,
    description  varchar(255),
    image_url    varchar(255),
    muscle_group varchar(255) not null
        constraint exercise_muscle_group_check
            check ((muscle_group)::text = ANY
        ((ARRAY ['CHEST'::character varying, 'BACK'::character varying, 'LEGS'::character varying, 'ARMS'::character varying, 'SHOULDERS'::character varying, 'CORE'::character varying])::text[])),
    name         varchar(255) not null
        constraint uk_r1ox34byx8pj01qd39plfobsj
            unique,
    video_url    varchar(255)
);

create table if not exists public.exercise_result (
                                        id character varying(255) primary key not null,
                                        date date,
                                        number_of_repetitions smallint,
                                        number_of_sets smallint,
                                        weight double precision not null,
                                        exercise_id character varying(255),
                                        user_id character varying(255),
                                        foreign key (exercise_id) references public.exercise (id)
                                            match simple on update no action on delete no action,
                                        foreign key (user_id) references public.users (id)
                                            match simple on update no action on delete no action
);