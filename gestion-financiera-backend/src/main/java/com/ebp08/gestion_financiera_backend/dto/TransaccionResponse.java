package com.ebp08.gestion_financiera_backend.dto;

import com.ebp08.gestion_financiera_backend.entity.Alerta;
import com.ebp08.gestion_financiera_backend.entity.Transaccion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransaccionResponse {
    private Transaccion transaccion;
    private List<Alerta> alertasGeneradas;
}
