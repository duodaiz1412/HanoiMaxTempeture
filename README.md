# Bài 12: Tìm nhiệt độ cao nhất theo từng năm bằng MapReduce

## Mô tả bài toán

Sử dụng framework Apache Hadoop MapReduce để tìm **nhiệt độ cao nhất (tmax)** theo từng năm từ dữ liệu thời tiết trạm Hà Nội (WMO 48820), giai đoạn 2000–2026.

## Dataset

- **Nguồn**: Meteostat — trạm Hà Nội (mã 48820)
- **File**: `input/hanoi-all.csv`
- **Số dòng**: 9.570 (bao gồm header)
- **Giai đoạn**: 2000 – 2026

Format CSV:

```
year,month,day,tmax
2017,6,21,41.0
2017,7,15,38.5
2020,6,10,39.0
```

## Cấu trúc project

```
weather-mapreduce/
├── src/
│   ├── MaxTemperature.java            # Driver — cấu hình và chạy Job
│   ├── MaxTemperatureMapper.java      # Mapper — đọc CSV, emit (year, tmax)
│   └── MaxTemperatureReducer.java     # Reducer — tìm max(tmax) mỗi năm
├── input/
│   └── hanoi-all.csv                  # Dữ liệu đầu vào
├── output/
│   └── part-r-00000                   # Kết quả đầu ra
├── build/                             # Class files đã biên dịch
├── MaxTemperature.jar                 # File JAR chạy trên Hadoop
└── README.md
```

## Mô tả hàm Map và Reduce

### Hàm Map

- **Input**: Từng dòng CSV (year, month, day, tmax)
- **Xử lý**: Tách cột `year` (index 0) và `tmax` (index 3), bỏ qua header và dòng thiếu dữ liệu
- **Output**: Cặp `(key, value)` = `(year, tmax)`

```
Dòng "2017,6,21,41.0"  →  emit("2017", 41.0)
Dòng "2017,7,15,38.5"  →  emit("2017", 38.5)
Dòng "2020,6,10,39.0"  →  emit("2020", 39.0)
```

### Shuffle & Sort (Hadoop framework tự xử lý)

Gom tất cả value có cùng key lại:

```
"2017" → [41.0, 38.5, 39.2, 37.8, ...]
"2020" → [39.0, 36.5, 38.0, ...]
```

### Hàm Reduce

- **Input**: Một key (year) và danh sách tất cả tmax của năm đó
- **Xử lý**: Duyệt danh sách, tìm giá trị lớn nhất
- **Output**: Cặp `(year, max_tmax)`

```
"2017", [41.0, 38.5, 39.2, ...]  →  output("2017", 41.0)
"2020", [39.0, 36.5, 38.0, ...]  →  output("2020", 39.0)
```

### Combiner

Sử dụng chính Reducer làm Combiner để tìm max cục bộ tại mỗi Mapper trước khi gửi sang Reducer, giúp giảm lượng dữ liệu truyền qua mạng.

## Luồng xử lý tổng thể

```
    Input CSV (9.570 dòng)
              │
              ▼
    ┌── MAPPER ──────────────────┐
    │  Đọc từng dòng CSV         │
    │  Emit (year, tmax)         │
    │  VD: ("2017", 41.0)       │
    └────────────┬───────────────┘
                 ▼
    ┌── COMBINER ────────────────┐
    │  Tìm max cục bộ            │
    │  ("2017", 41.0)           │
    └────────────┬───────────────┘
                 ▼
    ┌── SHUFFLE & SORT ──────────┐
    │  Gom value theo key (năm)  │
    │  "2017" → [41.0]          │
    └────────────┬───────────────┘
                 ▼
    ┌── REDUCER ─────────────────┐
    │  Tìm max toàn cục          │
    │  "2017" → 41.0            │
    └────────────┬───────────────┘
                 ▼
         Output: part-r-00000
```

## Yêu cầu hệ thống

- Java JDK 11
- Apache Hadoop 3.x (Standalone mode)

## Hướng dẫn chạy

```bash
# Vào thư mục project
cd ~/bigdata-lab/weather-mapreduce

# Biên dịch mã nguồn
javac -classpath "$(hadoop classpath)" -d build src/*.java

# Đóng gói JAR
jar -cvf MaxTemperature.jar -C build .

# Xoá output cũ (nếu có) và chạy MapReduce
rm -rf output
hadoop jar MaxTemperature.jar MaxTemperature input output

# Xem kết quả
cat output/part-r-00000
```

## Kết quả

| Năm  | Tmax (°C) | Năm  | Tmax (°C) |
|------|-----------|------|-----------|
| 2000 | 38.0      | 2014 | 38.0      |
| 2001 | 37.2      | 2015 | 39.0      |
| 2002 | 38.5      | 2016 | 39.0      |
| 2003 | 40.0      | 2017 | **41.0**  |
| 2004 | 38.6      | 2018 | 39.0      |
| 2005 | 38.5      | 2019 | 40.0      |
| 2006 | 39.0      | 2020 | 39.0      |
| 2007 | 38.9      | 2021 | 40.0      |
| 2008 | 37.8      | 2022 | 39.0      |
| 2009 | 38.8      | 2023 | 39.0      |
| 2010 | 40.4      | 2024 | 40.0      |
| 2011 | 39.0      | 2025 | 39.0      |
| 2012 | 39.0      | 2026 | 39.0      |
| 2013 | 39.0      |      |           |

Năm nóng nhất: **2017** với **41.0°C**.
