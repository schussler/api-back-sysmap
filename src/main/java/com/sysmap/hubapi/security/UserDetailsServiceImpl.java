package com.sysmap.hubapi.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) {
        // TODO: carregar usuário pelo email para autenticação
        throw new UnsupportedOperationException("Não implementado");
    }
}
