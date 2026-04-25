package com.techstore.techstore.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_model", columnNames = "model")
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Tên và mã model
    @Column(nullable = false, length = 150)
    private String name; // ví dụ: iPhone 15

    @Column(nullable = false, length = 100)
    private String model; // ví dụ: IP15

    // 🔹 Giá cơ bản (có thể dùng làm giá mặc định)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    // 🔹 Tồn kho tổng (nếu không có variant riêng)
    @Column(nullable = false)
    private Integer stock = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String images; // có thể là danh sách URL, ngăn cách bằng dấu phẩy

    // 🔹 Thông tin kỹ thuật chung
    @Column(length = 50)
    private String ram;

    @Column(length = 100)
    private String display;

    @Column(length = 150)
    private String camera;

    @Column(length = 50)
    private String battery;

    @Column(length = 100)
    private String dimensions;

    @Column(length = 100)
    private String material;

    @CreationTimestamp
    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // 🔹 Liên kết với thương hiệu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    @JsonIgnore
    private Brand brand;

    // 🔹 Danh sách biến thể (color, storage, giá, tồn kho riêng)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "sale_percent", nullable = false)
    private Integer salePercent = 0;

    @Column(name = "badge")
    private String badge; // HOT / NEW / SALE...

    @Column(length = 150)
    private String origin; // Nguồn nhập hàng (ví dụ: Nhật, Mỹ, Chính hãng VN, Nhập khẩu)



    public Product() {}

    public Product(Long id, String name, String model, BigDecimal price, Integer stock, String description, String images, String ram, String display, String camera, String battery, String dimensions, String material, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isDeleted, Brand brand, List<ProductVariant> variants, Integer salePercent, String badge, String origin) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.images = images;
        this.ram = ram;
        this.display = display;
        this.camera = camera;
        this.battery = battery;
        this.dimensions = dimensions;
        this.material = material;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.brand = brand;
        this.variants = variants;
        this.salePercent = salePercent;
        this.badge = badge;
        this.origin = origin;
    }

    // ===============================================================
    // ✅ Helper Methods (quản lý quan hệ OneToMany)
    public BigDecimal getFinalPrice() {
        if (salePercent == null || salePercent <= 0) {
            return price;
        }

        BigDecimal discount = price.multiply(BigDecimal.valueOf(salePercent))
                .divide(BigDecimal.valueOf(100));

        return price.subtract(discount);
    }


    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
        updateTotalStock();
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
        updateTotalStock();
    }
    public void updateTotalStock() {
        if (variants == null || variants.isEmpty()) {
            this.stock = 0;
        } else {
            this.stock = variants.stream()
                    .map(ProductVariant::getStock)
                    .reduce(0, Integer::sum);
        }
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    // ✅ Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = (stock != null) ? stock : 0; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }

    public String getCamera() { return camera; }
    public void setCamera(String camera) { this.camera = camera; }

    public String getBattery() { return battery; }
    public void setBattery(String battery) { this.battery = battery; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getSalePercent() {
        return salePercent;
    }

    public void setSalePercent(Integer salePercent) {
        this.salePercent = salePercent;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    // ===============================================================
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", model='" + model + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", ram='" + ram + '\'' +
                ", display='" + display + '\'' +
                ", camera='" + camera + '\'' +
                ", variants=" + variants.size() +
                '}';
    }
}
