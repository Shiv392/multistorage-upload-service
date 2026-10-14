# Multi-Storage-Upload-Service(Backend)

Same file, three places to store it. This Spring Boot backend uploads a file using three different strategies so you can compare how each one behaves.

| Strategy | Where the file goes | What the database stores |
|----------|--------------------|--------------------------|
| **Disk** | A folder on the server | File info + local path |
| **DB** | A `bytea` column in PostgreSQL | File info + the file bytes |
| **S3** | A Supabase Storage bucket (S3-compatible) | File info + the object key |

## Tech stack

- Java 17+
- Spring Boot (Web, Data JPA)
- PostgreSQL hosted on **Supabase**
- Supabase Storage (S3-compatible API) for the S3 strategy
- Maven

## Why this project exists

Before using cloud storage, it helps to feel the problems of the older approaches:

- Files in the DB make the database heavy and backups slow.
- Files on the server disk can disappear on redeploy and break when you run more than one server.
- Object storage (S3) keeps files separate from the app and the database.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/files/disk` | Save the file to a server folder, store the path in the DB |
| `POST` | `/api/files/db` | Save the file bytes directly in PostgreSQL |
| `POST` | `/api/files/s3` | Upload the file to the Supabase bucket, store the key in the DB |
| `GET`  | `/api/files/{id}` | Fetch a file. The source is chosen using `storage_type` |

All upload endpoints take `multipart/form-data` with a single field named `file`.

### Example request

```bash
curl -X POST http://localhost:8080/api/files/disk \
  -F "file=@./photo.png"
```

### Example response

```json
{
  "id": 1,
  "fileName": "photo.png",
  "fileType": "image/png",
  "size": 204800,
  "storageType": "DISK"
}
```

## Database table

```sql
CREATE TABLE files (
    id            BIGSERIAL PRIMARY KEY,
    file_name     VARCHAR(255) NOT NULL,
    file_type     VARCHAR(100) NOT NULL,
    size          BIGINT       NOT NULL,
    storage_type  VARCHAR(10)  NOT NULL,   -- DISK | DB | S3
    file_path     TEXT,                    -- used by DISK
    s3_key        TEXT,                    -- used by S3
    data          BYTEA,                   -- used by DB
    created_at    TIMESTAMP DEFAULT now()
);
```

## Setup

### 1. Create a Supabase project

1. Go to [supabase.com](https://supabase.com) and create a project.
2. Open **Project Settings → Database** and copy the connection details (host, port, database, user, password).
3. Run the SQL above in the **SQL Editor** to create the `files` table.

### 2. Create a storage bucket

1. Open **Storage** and create a bucket, for example `uploads`.
2. Keep it **private** (files are served through the API or signed URLs).
3. Open **Project Settings → Storage → S3 Connection** and note:
   - the S3 endpoint
   - the region
   - then generate an **access key ID** and **secret access key**

### 3. Configure the app

Create `src/main/resources/application.properties`:

```properties
# Database (Supabase PostgreSQL)
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# Upload limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Disk storage
app.storage.disk.path=${UPLOAD_DIR:./uploads}

# Supabase Storage (S3 compatible)
app.storage.s3.endpoint=${S3_ENDPOINT}
app.storage.s3.region=${S3_REGION}
app.storage.s3.bucket=${S3_BUCKET}
app.storage.s3.access-key=${S3_ACCESS_KEY}
app.storage.s3.secret-key=${S3_SECRET_KEY}
```

Set the values as environment variables instead of writing them in the file:

```bash
export DB_HOST=db.xxxxx.supabase.co
export DB_PORT=5432
export DB_NAME=postgres
export DB_USER=postgres
export DB_PASSWORD=your-password

export S3_ENDPOINT=https://xxxxx.supabase.co/storage/v1/s3
export S3_REGION=your-region
export S3_BUCKET=uploads
export S3_ACCESS_KEY=your-access-key
export S3_SECRET_KEY=your-secret-key
```

> **Never commit secrets.** Add `application.properties` or any `.env` file to `.gitignore` if it holds real values.

### 4. Run

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## Project structure (suggested)

```
src/main/java/com/example/storageshowdown
├── controller   # FileController (REST endpoints)
├── service      # DiskStorageService, DbStorageService, S3StorageService
├── entity       # FileEntity
├── repository   # FileRepository
└── config       # S3 client configuration
```

## How a file is served

`GET /api/files/{id}` looks up the row, checks `storage_type`, reads the file from the right place, and responds with:

- `Content-Type`: the saved file type, so the browser knows how to render it
- `Content-Disposition: inline`: show it in the browser instead of downloading

## Security notes

- Never use the original file name as the stored name. Generate a UUID instead to avoid path traversal.
- Validate the file type and size on the server. Do not trust the frontend alone.
- Keep the bucket private and do not expose the S3 keys to the frontend.

## Things to try

Upload the same 5 MB image through all three endpoints, then compare:

1. How much did the database size grow?
2. How long did each upload take?
3. After restarting the server, do all three files still load?
4. What happens if you deploy to a platform with a temporary disk?

## Author

Built while learning how file uploads work, from Angular to Spring Boot to storage.
