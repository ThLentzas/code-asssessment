package gr.aegean.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@AllArgsConstructor
public class User implements UserDetails {
    private Integer id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String password;
    private String bio;
    private String location;
    private String company;

    public User(String firstname,
                String lastname,
                String username,
                String email,
                String password,
                String bio,
                String location,
                String company) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.location = location;
        this.company = company;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(
            Integer id,
            String firstname,
            String lastname,
            String username,
            String email,
            String bio,
            String location,
            String company) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.location = location;
        this.company = company;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}