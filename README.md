# Smart Doctor Appointment Booking and Patient Management System

Android Studio project (Java, XML, RecyclerView, Glide, SharedPreferences) wired to **Supabase**
as the backend, following the module spec.

## 1. What's fully working

- **Auth**: Register, Login, Forgot Password, Logout (Supabase Auth REST, session in SharedPreferences)
- **Dashboard**: live counts (total/upcoming appointments, records), categories, popular doctors, search
- **Doctor Categories / List / Profile**: real Supabase queries, search, filters (top rated / experience / fee), Glide images
- **Appointment Booking**: date picker + time slots + insert into `appointments`
- **Appointment History**: list + cancel (status PATCH)
- **Medical Records**: list + a simple "add record" dialog (name + file URL) that inserts a row
- **Reviews & Ratings**: list + submit a rating/review per doctor
- **Notifications**: list from a `notifications` table
- **Profile**: view/edit profile fields, change password (Supabase Auth), logout

## 2. What's intentionally stubbed (extend as needed)

- **Favorites**: no table in the original spec's DB design — add a `favorites` table
  (`patient_id`, `doctor_id`) and wire `btnAddFavorite` / the favorites dashboard card to it.
- **File uploads** for medical records / profile photo: currently you type/paste a URL.
  To do real uploads, use Supabase Storage's REST API (`POST /storage/v1/object/{bucket}/{path}`)
  with the same access token, then save the returned public URL into `file_url` / `profile_image`.
- **Push notifications, Google Maps, video consultation, QR pass**: optional/bonus items from
  the spec, not implemented.
- **Doctor-side app / admin**: this project is the **patient-facing app only**, per the spec's modules.

## 3. Supabase setup

1. Create a project at https://supabase.com.
2. In **Project Settings → API**, copy the **Project URL** and **anon public key** into
   `app/src/main/java/com/example/doctorapp/config/Config.java`.
3. In the **SQL Editor**, run the schema below.
4. In **Authentication → Settings**, you can disable "Confirm email" while testing so signup
   returns a usable session immediately (otherwise users must confirm by email before logging in).

```sql
-- USERS (profile fields mirrored from auth.users)
create table public.users (
  id uuid primary key references auth.users(id) on delete cascade,
  name text,
  email text,
  mobile text,
  gender text,
  dob text,
  profile_image text,
  address text,
  blood_group text,
  created_at timestamp with time zone default now()
);

-- DOCTORS
create table public.doctors (
  id uuid primary key default gen_random_uuid(),
  doctor_name text not null,
  specialization text,
  experience int,
  fee numeric,
  hospital_name text,
  availability text,
  rating numeric default 0,
  image text,
  qualification text
);

-- APPOINTMENTS
create table public.appointments (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid references public.users(id),
  doctor_id uuid references public.doctors(id),
  doctor_name text,
  appointment_date text,
  appointment_time text,
  status text default 'Pending',
  created_at timestamp with time zone default now()
);

-- MEDICAL RECORDS
create table public.medical_records (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid references public.users(id),
  doctor_id uuid references public.doctors(id),
  report_name text,
  report_date text,
  file_url text
);

-- REVIEWS
create table public.reviews (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid references public.users(id),
  doctor_id uuid references public.doctors(id),
  patient_name text,
  rating int,
  review text,
  created_at timestamp with time zone default now()
);

-- NOTIFICATIONS
create table public.notifications (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.users(id),
  title text,
  message text,
  status text default 'unread',
  created_at timestamp with time zone default now()
);

-- Row Level Security: every patient can only read/write their own rows.
alter table public.users enable row level security;
alter table public.appointments enable row level security;
alter table public.medical_records enable row level security;
alter table public.reviews enable row level security;
alter table public.notifications enable row level security;
alter table public.doctors enable row level security;

create policy "Users manage own row" on public.users
  for all using (auth.uid() = id) with check (auth.uid() = id);

create policy "Patients manage own appointments" on public.appointments
  for all using (auth.uid() = patient_id) with check (auth.uid() = patient_id);

create policy "Patients manage own records" on public.medical_records
  for all using (auth.uid() = patient_id) with check (auth.uid() = patient_id);

create policy "Anyone can read reviews, patients write own" on public.reviews
  for select using (true);
create policy "Patients insert own reviews" on public.reviews
  for insert with check (auth.uid() = patient_id);

create policy "Patients read own notifications" on public.notifications
  for select using (auth.uid() = user_id);

create policy "Anyone can read doctors" on public.doctors
  for select using (true);
```

5. Insert a few sample doctors so the app has data to show, e.g.:

```sql
insert into public.doctors (doctor_name, specialization, experience, fee, hospital_name, rating, qualification)
values
('Dr. Sharma', 'Cardiologist', 12, 500, 'City Hospital', 4.7, 'MD'),
('Dr. Iyer', 'Dermatologist', 8, 400, 'Apollo Clinic', 4.5, 'MBBS, DVD'),
('Dr. Khan', 'General Physician', 15, 300, 'Sunrise Hospital', 4.8, 'MD');
```

## 4. Build & run

Open the `DoctorAppointmentApp` folder in Android Studio (it will offer to sync Gradle and
download the dependencies above), fill in `Config.java`, then Run on an emulator or device.

## 5. Project structure

```
app/src/main/java/com/example/doctorapp/
  config/Config.java                 Supabase URL + anon key
  network/                           OkHttp client, ApiCallback, SupabaseAuthManager
  utils/                             SessionManager (SharedPreferences), Constants
  models/                            POJOs matching the DB tables in the spec
  adapters/                          RecyclerView adapters (Doctor, Category, Appointment, ...)
  ui/splash, auth, main, doctor, appointment, records, reviews, notifications, profile
```

## 6. Recommended fix: auto-create profile via DB trigger

Inserting the profile row from the app (as the README originally showed) fights Row Level
Security, because at signup time the request is made as the anonymous role, not as the new user.
Use this trigger instead — it runs with elevated privilege and fires the instant the auth user
is created, regardless of email-confirmation status:

```sql
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.users (id, name, email, mobile, gender, dob)
  values (
    new.id,
    new.raw_user_meta_data->>'full_name',
    new.email,
    new.raw_user_meta_data->>'mobile',
    new.raw_user_meta_data->>'gender',
    new.raw_user_meta_data->>'dob'
  );
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();
```

For this to populate `mobile`/`gender`/`dob`, the signup request must pass them as `user_metadata`
(see `SupabaseAuthManager.signUp` and `RegisterActivity`, updated to send a `data` object).
