import React from 'react';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
}

interface CardHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
}

interface CardTitleProps extends React.HTMLAttributes<HTMLHeadingElement> {
}

interface CardContentProps extends React.HTMLAttributes<HTMLDivElement> {
}

export const Card: React.FC<CardProps> = ({
                                              className = '',
                                              children,
                                              ...props
                                          }) => (
    <div
        className={`rounded-lg border border-gray-700 bg-gray-800 text-gray-100 shadow ${className}`}
        {...props}
    >
        {children}
    </div>
);

export const CardHeader: React.FC<CardHeaderProps> = ({
                                                          className = '',
                                                          children,
                                                          ...props
                                                      }) => (
    <div
        className={`flex flex-col space-y-1.5 p-6 ${className}`}
        {...props}
    >
        {children}
    </div>
);

export const CardTitle: React.FC<CardTitleProps> = ({
                                                        className = '',
                                                        children,
                                                        ...props
                                                    }) => (
    <h3
        className={`text-2xl font-semibold leading-none tracking-tight ${className}`}
        {...props}
    >
        {children}
    </h3>
);

export const CardContent: React.FC<CardContentProps> = ({
                                                            className = '',
                                                            children,
                                                            ...props
                                                        }) => (
    <div
        className={`p-6 pt-0 ${className}`}
        {...props}
    >
        {children}
    </div>
);

// Also export a combined object for convenience
export const Cards = {
    Root: Card,
    Header: CardHeader,
    Title: CardTitle,
    Content: CardContent,
};

export default Cards;