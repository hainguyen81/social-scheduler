# 📚 Technical Documentation: social-scheduler

## 📖 1. Executive Summary

**Document ID:** `DOC-001`  
**Version:** 1.0 (Cơ sở)  
**Date:** 2026/09/12 13:29:15  
**Author:** Enterprise System Architect (SA Agent)  
**Target Path:** `./sources/docs/technical-documentation.md`

### 🎯 Mục tiêu Hệ thống
Hệ thống social-scheduler là một nền tảng quản lý lịch đăng bài tự động và đề xuất nội dung được thiết kế theo kiến trúc microservices. Nó hỗ trợ việc lên lịch bài đăng trên các nền tảng mạng xã hội (Facebook, Instagram, TikTok), đề xuất nội dung dựa trên AI, và thực hiện kiểm soát tỷ lệ để đảm bảo sử dụng công bằng.

### 🏗️ Kiến trúc Tổng quan
- **Kiến trúc:** Event-Driven Architecture (EDA) với Kafka làm trung tâm message broker
- **Kiến trúc dữ liệu:** Command Query Responsibility Segregation (CQRS) với Spring Data JPA
- **Mô hình lập trình:** Reactive Programming cho xử lý bất đồng bộ
- **Đa tenant:** Hỗ trợ nhiều tenant với cách ly dữ liệu nghiêm ngặt

---

## 🏗️ 2. Kiến trúc Hệ thống

### 2.1 Sơ đồ Kiến trúc (Mermaid)