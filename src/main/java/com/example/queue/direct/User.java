package com.example.queue.direct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * TODO
 *
 * @author admin
 * @version 1.0
 * @date 2021/2/3 14:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private String Username;
    private String password;
}
