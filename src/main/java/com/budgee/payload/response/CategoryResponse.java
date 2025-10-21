package com.budgee.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

import com.budgee.enums.TransactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse implements Serializable {

    UUID categoryId;
    String name;
    TransactionType type;
    String description;
    String color;
    String icon;
    Boolean editable;
    Boolean deletable;
}
