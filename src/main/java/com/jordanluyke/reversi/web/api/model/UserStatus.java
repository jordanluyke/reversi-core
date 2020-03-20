package com.jordanluyke.reversi.web.api.model;

import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
public class UserStatus {

    private Status status = Status.ACTIVE;
    private Subject<Status> onChange = PublishSubject.create();
    private int disconnectChecksRemaining = 3;

    public void reset() {
        status = Status.ACTIVE;
        onChange.onNext(Status.ACTIVE);
        disconnectChecksRemaining = 3;
    }

    public enum Status {
        ACTIVE,
        IDLE,
        OFFLINE
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "status=" + status +
                ", onChange=" + onChange +
                ", disconnectChecksRemaining=" + disconnectChecksRemaining +
                '}';
    }
}
